package com.example.backend.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.province.ProvinceCache;
import com.example.backend.report.ReportService;
import com.example.backend.student.BlockTotalCache;
import com.example.backend.subject.Subject;
import com.example.backend.subject.SubjectRepository;
import com.example.backend.subject.SubjectRegistry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.health.contributor.Status;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

class CsvDataSeederAndHealthTests {

    private static final String SAMPLE_CSV = """
            sbd,toan,ngu_van,ngoai_ngu,vat_li,hoa_hoc,sinh_hoc,lich_su,dia_li,gdcd,ma_ngoai_ngu
            01000001,8.4,6.75,8.0,6.0,5.25,5.0,,,,N1
            26020938,9.6,8.0,9.6,10.0,10.0,8.75,,,,N1
            """;

    private ImporterProperties importerProperties;
    private CsvRowParser csvRowParser;
    private SubjectRepository subjectRepository;
    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;
    private BlockTotalCache blockTotalCache;
    private ProvinceCache provinceCache;
    private ReportService reportService;
    private DataSeedState dataSeedState;
    private DataSeederHealthIndicator healthIndicator;
    private CsvDataSeeder seeder;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        importerProperties = new ImporterProperties();
        importerProperties.setEnabled(true);
        importerProperties.setCsvPath(tempDir.resolve("missing.csv").toString());
        importerProperties.setBatchSize(500);

        csvRowParser = new CsvRowParser(new SubjectRegistry());
        subjectRepository = mock(SubjectRepository.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        transactionTemplate = new TransactionTemplate() {
            @Override
            public void executeWithoutResult(Consumer<TransactionStatus> action) {
                action.accept(null);
            }
        };
        blockTotalCache = mock(BlockTotalCache.class);
        provinceCache = mock(ProvinceCache.class);
        reportService = mock(ReportService.class);
        dataSeedState = new DataSeedState();
        healthIndicator = new DataSeederHealthIndicator(dataSeedState);

        seeder = new CsvDataSeeder(
                importerProperties,
                csvRowParser,
                subjectRepository,
                jdbcTemplate,
                transactionTemplate,
                blockTotalCache,
                provinceCache,
                reportService,
                dataSeedState);

        when(subjectRepository.findAll()).thenReturn(List.of(
                Subject.builder().id(1L).code("TOAN").name("Toán").build(),
                Subject.builder().id(2L).code("NGU_VAN").name("Ngữ văn").build(),
                Subject.builder().id(3L).code("NGOAI_NGU").name("Ngoại ngữ").build(),
                Subject.builder().id(4L).code("VAT_LI").name("Vật lí").build(),
                Subject.builder().id(5L).code("HOA_HOC").name("Hóa học").build(),
                Subject.builder().id(6L).code("SINH_HOC").name("Sinh học").build(),
                Subject.builder().id(7L).code("LICH_SU").name("Lịch sử").build(),
                Subject.builder().id(8L).code("DIA_LI").name("Địa lí").build(),
                Subject.builder().id(9L).code("GDCD").name("GDCD").build()));
    }

    @Test
    void emptyDatabaseWithImporterDisabledShouldNotMarkReady() {
        importerProperties.setEnabled(false);
        stubMetadata(Optional.empty());
        stubTableCounts(0L, 0L, 0L);

        seeder.run(new DefaultApplicationArguments());

        assertEquals(SeedState.NOT_READY_EMPTY_DB, dataSeedState.getState());
        assertFalse(dataSeedState.isDataReady());
        assertEquals(Status.OUT_OF_SERVICE, healthIndicator.health().getStatus());
    }

    @Test
    void emptyDatabaseWithMissingCsvShouldNotMarkReady() {
        importerProperties.setEnabled(true);
        importerProperties.setCsvPath(tempDir.resolve("does_not_exist.csv").toString());
        stubMetadata(Optional.empty());
        stubTableCounts(0L, 0L, 0L);

        seeder.run(new DefaultApplicationArguments());

        assertEquals(SeedState.NOT_READY_EMPTY_DB, dataSeedState.getState());
        assertFalse(dataSeedState.isDataReady());
        assertEquals(Status.OUT_OF_SERVICE, healthIndicator.health().getStatus());
    }

    @Test
    void unverifiedLegacyDatabaseWithoutCsvShouldFailClosed() {
        importerProperties.setEnabled(true);
        importerProperties.setCsvPath(tempDir.resolve("no_csv_in_container.csv").toString());
        stubMetadata(Optional.of(new MetadataStub("UNVERIFIED_LEGACY", 100L, 600L, 250L)));
        stubTableCounts(100L, 600L, 250L);

        seeder.run(new DefaultApplicationArguments());

        assertEquals(SeedState.NOT_READY_UNVERIFIED_DATA, dataSeedState.getState());
        assertFalse(dataSeedState.isDataReady());
        assertEquals(Status.OUT_OF_SERVICE, healthIndicator.health().getStatus());
        verify(blockTotalCache, never()).refresh();
    }

    @Test
    void unverifiedLegacyDatabaseWithImporterDisabledShouldFailClosedEvenIfCsvExists() throws IOException {
        Path csvFile = tempDir.resolve("diem_thi_thpt_2024.csv");
        Files.writeString(csvFile, SAMPLE_CSV, StandardCharsets.UTF_8);

        importerProperties.setEnabled(false);
        importerProperties.setCsvPath(csvFile.toString());
        stubMetadata(Optional.of(new MetadataStub("UNVERIFIED_LEGACY", 2L, 12L, 8L)));
        stubTableCounts(2L, 12L, 8L);

        seeder.run(new DefaultApplicationArguments());

        assertEquals(SeedState.NOT_READY_UNVERIFIED_DATA, dataSeedState.getState());
        assertFalse(dataSeedState.isDataReady());
        assertEquals(Status.OUT_OF_SERVICE, healthIndicator.health().getStatus());
    }

    @Test
    void unverifiedLegacyDatabaseWithImporterEnabledAndCsvShouldCleanAndReimport() throws IOException {
        Path csvFile = tempDir.resolve("diem_thi_thpt_2024.csv");
        Files.writeString(csvFile, SAMPLE_CSV, StandardCharsets.UTF_8);

        importerProperties.setEnabled(true);
        importerProperties.setCsvPath(csvFile.toString());
        stubMetadata(Optional.of(new MetadataStub("UNVERIFIED_LEGACY", 2L, 12L, 8L)));
        stubTableCountsSequence(
                new long[]{2L, 2L},
                new long[]{12L, 12L},
                new long[]{8L, 8L});

        seeder.run(new DefaultApplicationArguments());

        assertEquals(SeedState.READY_IMPORTED, dataSeedState.getState());
        assertTrue(dataSeedState.isDataReady());
        assertEquals(Status.UP, healthIndicator.health().getStatus());
        verify(jdbcTemplate).execute(argThat((String sql) -> sql != null && sql.startsWith("TRUNCATE TABLE")));
        verify(jdbcTemplate).update(
                argThat((String sql) -> sql != null && sql.contains("SET status = 'COMPLETED'")),
                anyString(),
                argThat((Object checksum) -> checksum instanceof String s && s.length() == 64),
                any(),
                any(),
                any(),
                any(),
                any(),
                anyString());
        verify(blockTotalCache).refresh();
        verify(provinceCache).refresh();
        verify(reportService, atLeastOnce()).clearCaches();
    }

    @Test
    void completedMetadataWithMatchingCountsShouldMarkReadyExistingRegardlessOfImporterFlag() {
        importerProperties.setEnabled(false);
        stubMetadata(Optional.of(new MetadataStub("COMPLETED", 1061605L, 6037435L, 2627815L)));
        stubTableCounts(1061605L, 6037435L, 2627815L);

        seeder.run(new DefaultApplicationArguments());

        assertEquals(SeedState.READY_EXISTING_DATA, dataSeedState.getState());
        assertTrue(dataSeedState.isDataReady());
        assertEquals(Status.UP, healthIndicator.health().getStatus());
        verify(blockTotalCache).refresh();
        verify(provinceCache).refresh();
        verify(reportService).clearCaches();
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void completedMetadataWithMismatchedCountsShouldMarkDataInconsistentAndDown() {
        importerProperties.setEnabled(true);
        stubMetadata(Optional.of(new MetadataStub("COMPLETED", 1061605L, 6037435L, 2627815L)));
        // Missing 100 student_block_scores rows
        stubTableCounts(1061605L, 6037435L, 2627715L);

        seeder.run(new DefaultApplicationArguments());

        assertEquals(SeedState.DATA_INCONSISTENT, dataSeedState.getState());
        assertFalse(dataSeedState.isDataReady());
        assertEquals(Status.DOWN, healthIndicator.health().getStatus());
        verify(jdbcTemplate, never()).execute(argThat((String sql) -> sql != null && sql.startsWith("TRUNCATE")));
        verify(jdbcTemplate).update(
                argThat((String sql) -> sql != null && sql.contains("VALUES (?, 'FAILED'")),
                anyString(),
                anyString());
        verify(reportService).clearCaches();
    }

    @Test
    void inProgressMetadataFromCrashedImportShouldCleanAndReimportWhenCsvAvailable() throws IOException {
        Path csvFile = tempDir.resolve("diem_thi_thpt_2024.csv");
        Files.writeString(csvFile, SAMPLE_CSV, StandardCharsets.UTF_8);

        importerProperties.setEnabled(true);
        importerProperties.setCsvPath(csvFile.toString());
        stubMetadata(Optional.of(new MetadataStub("IN_PROGRESS", 0L, 0L, 0L)));
        stubTableCountsSequence(
                new long[]{1L, 2L},
                new long[]{6L, 12L},
                new long[]{0L, 8L});

        seeder.run(new DefaultApplicationArguments());

        assertEquals(SeedState.READY_IMPORTED, dataSeedState.getState());
        assertTrue(dataSeedState.isDataReady());
        assertEquals(Status.UP, healthIndicator.health().getStatus());
        verify(jdbcTemplate).execute(argThat((String sql) -> sql != null && sql.startsWith("TRUNCATE TABLE")));
    }

    @Test
    void freshImportShouldSucceedEvenIfAnalyzeLacksPermissions() throws IOException {
        Path csvFile = tempDir.resolve("diem_thi_thpt_2024.csv");
        Files.writeString(csvFile, SAMPLE_CSV, StandardCharsets.UTF_8);

        importerProperties.setEnabled(true);
        importerProperties.setCsvPath(csvFile.toString());
        stubMetadata(Optional.empty());
        stubTableCountsSequence(
                new long[]{0L, 2L},
                new long[]{0L, 12L},
                new long[]{0L, 8L});

        doThrow(new PermissionDeniedDataAccessException("permission denied", null))
                .when(jdbcTemplate)
                .execute(argThat((String sql) -> sql != null && sql.startsWith("ANALYZE ")));

        seeder.run(new DefaultApplicationArguments());

        assertEquals(SeedState.READY_IMPORTED, dataSeedState.getState());
        assertTrue(dataSeedState.isDataReady());
        assertEquals(Status.UP, healthIndicator.health().getStatus());
        verify(reportService, atLeastOnce()).clearCaches();
    }

    @Test
    void precomputeFailureShouldMarkSeedStateFailedAndHealthDown() throws IOException {
        Path csvFile = tempDir.resolve("diem_thi_thpt_2024.csv");
        Files.writeString(csvFile, SAMPLE_CSV, StandardCharsets.UTF_8);

        importerProperties.setEnabled(true);
        importerProperties.setCsvPath(csvFile.toString());
        stubMetadata(Optional.empty());
        stubTableCounts(0L, 0L, 0L);

        when(jdbcTemplate.update(argThat((String sql) -> sql != null && sql.contains("INSERT INTO student_block_scores"))))
                .thenThrow(new DataIntegrityViolationException("Simulated precompute SQL error"));

        seeder.run(new DefaultApplicationArguments());

        assertEquals(SeedState.FAILED, dataSeedState.getState());
        assertFalse(dataSeedState.isDataReady());
        assertEquals(Status.DOWN, healthIndicator.health().getStatus());
    }

    @SuppressWarnings("unchecked")
    private void stubMetadata(Optional<MetadataStub> metadataOpt) {
        when(jdbcTemplate.query(
                argThat((String sql) -> sql != null && sql.contains("FROM import_metadata")),
                any(PreparedStatementSetter.class),
                any(ResultSetExtractor.class)))
                .thenAnswer(inv -> {
                    ResultSetExtractor<Object> extractor = inv.getArgument(2);
                    ResultSet rs = mock(ResultSet.class);
                    if (metadataOpt.isEmpty()) {
                        when(rs.next()).thenReturn(false);
                    } else {
                        MetadataStub m = metadataOpt.get();
                        when(rs.next()).thenReturn(true);
                        try {
                            when(rs.getString("status")).thenReturn(m.status());
                            when(rs.getLong("imported_students")).thenReturn(m.students());
                            when(rs.getLong("imported_scores")).thenReturn(m.scores());
                            when(rs.getLong("precomputed_blocks")).thenReturn(m.blocks());
                        } catch (SQLException ignored) {
                            // Mockito stubbing
                        }
                    }
                    return extractor.extractData(rs);
                });
    }

    private void stubTableCounts(long students, long scores, long blocks) {
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Long.class)).thenReturn(students);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exam_scores", Long.class)).thenReturn(scores);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM student_block_scores", Long.class))
                .thenReturn(blocks);
    }

    private void stubTableCountsSequence(long[] studentsSeq, long[] scoresSeq, long[] blocksSeq) {
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Long.class))
                .thenReturn(studentsSeq[0], studentsSeq[1]);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exam_scores", Long.class))
                .thenReturn(scoresSeq[0], scoresSeq[1]);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM student_block_scores", Long.class))
                .thenReturn(blocksSeq[0], blocksSeq[1]);
    }

    private record MetadataStub(String status, long students, long scores, long blocks) {}
}

