package com.example.backend.importer;

import com.example.backend.province.ProvinceCache;
import com.example.backend.report.ReportService;
import com.example.backend.student.BlockTotalCache;
import com.example.backend.subject.Subject;
import com.example.backend.subject.SubjectRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvDataSeeder implements ApplicationRunner {

    static final String DATASET_KEY = "thpt_2024";

    private static final String INSERT_STUDENT_SQL =
            "INSERT INTO students (id, sbd, language_code, province_code) VALUES (?, ?, ?, ?) ON CONFLICT (sbd) DO NOTHING";

    private static final String INSERT_SCORE_SQL =
            "INSERT INTO exam_scores (student_id, subject_id, score) VALUES (?, ?, ?) "
                    + "ON CONFLICT (student_id, subject_id) DO NOTHING";

    private static final String SELECT_METADATA_SQL =
            "SELECT status, imported_students, imported_scores, precomputed_blocks "
                    + "FROM import_metadata WHERE dataset_key = ?";

    private final ImporterProperties importerProperties;
    private final CsvRowParser csvRowParser;
    private final SubjectRepository subjectRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final BlockTotalCache blockTotalCache;
    private final ProvinceCache provinceCache;
    private final ReportService reportService;
    private final DataSeedState dataSeedState;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Optional<ImportMetadataRow> metadataOpt = loadImportMetadata();
            long actualStudents = queryCount("students");
            long actualScores = queryCount("exam_scores");
            long actualBlocks = queryCount("student_block_scores");
            boolean hasAnyData = actualStudents > 0 || actualScores > 0 || actualBlocks > 0;

            if (metadataOpt.isPresent()) {
                ImportMetadataRow metadata = metadataOpt.get();
                if ("COMPLETED".equals(metadata.status())) {
                    boolean countsPositive = metadata.importedStudents() > 0
                            && metadata.importedScores() > 0
                            && metadata.precomputedBlocks() > 0;
                    boolean countsMatch = actualStudents == metadata.importedStudents()
                            && actualScores == metadata.importedScores()
                            && actualBlocks == metadata.precomputedBlocks();

                    if (countsPositive && countsMatch) {
                        log.info(
                                "Verified COMPLETED import metadata '{}': students={}, scores={}, blocks={}.",
                                DATASET_KEY,
                                actualStudents,
                                actualScores,
                                actualBlocks);
                        refreshAllCaches();
                        dataSeedState.markReadyExisting(
                                "Verified existing dataset (" + actualStudents + " students).");
                        return;
                    }

                    String mismatchDetail = String.format(
                            "Import consistency check failed for '%s': expected (students=%d, scores=%d, blocks=%d) "
                                    + "but found (students=%d, scores=%d, blocks=%d).",
                            DATASET_KEY,
                            metadata.importedStudents(),
                            metadata.importedScores(),
                            metadata.precomputedBlocks(),
                            actualStudents,
                            actualScores,
                            actualBlocks);
                    log.error(mismatchDetail);
                    markMetadataFailed(mismatchDetail);
                    reportService.clearCaches();
                    dataSeedState.markDataInconsistent(mismatchDetail);
                    return;
                }
            }

            boolean isUnverifiedOrIncompleteExisting = hasAnyData
                    || metadataOpt.map(m -> "UNVERIFIED_LEGACY".equals(m.status())).orElse(false);

            if (!importerProperties.isEnabled()) {
                if (isUnverifiedOrIncompleteExisting) {
                    String msg = "Existing dataset is unverified or incomplete ("
                            + metadataOpt.map(ImportMetadataRow::status).orElse("NO_METADATA")
                            + ") and CSV importer is disabled.";
                    log.warn(msg);
                    dataSeedState.markNotReadyUnverified(msg);
                } else {
                    String msg = "Database is empty and CSV importer is disabled.";
                    log.warn(msg);
                    dataSeedState.markNotReadyEmpty(msg);
                }
                return;
            }

            Optional<Path> csvPathOpt = resolveCsvPath(importerProperties.getCsvPath());
            if (csvPathOpt.isEmpty()) {
                if (isUnverifiedOrIncompleteExisting) {
                    String msg = "Existing dataset is unverified or incomplete ("
                            + metadataOpt.map(ImportMetadataRow::status).orElse("NO_METADATA")
                            + ") and CSV dataset file was not found at '" + importerProperties.getCsvPath() + "'.";
                    log.warn(msg);
                    dataSeedState.markNotReadyUnverified(msg);
                } else {
                    String msg = "Database is empty and CSV dataset file was not found at '"
                            + importerProperties.getCsvPath() + "'.";
                    log.warn(msg);
                    dataSeedState.markNotReadyEmpty(msg);
                }
                return;
            }

            boolean cleanExistingData = isUnverifiedOrIncompleteExisting || metadataOpt.isPresent();
            executeCsvImport(csvPathOpt.get(), cleanExistingData);
        } catch (Exception ex) {
            String errorMsg = "Fatal error during dataset verification or CSV import: " + ex.getMessage();
            log.error(errorMsg, ex);
            markMetadataFailedSafely(errorMsg);
            reportService.clearCaches();
            dataSeedState.markFailed(errorMsg);
        }
    }

    private void executeCsvImport(Path csvPath, boolean cleanExistingData) throws Exception {
        dataSeedState.markSeeding("Importing CSV dataset from " + csvPath.getFileName());
        reportService.clearCaches();

        Map<String, Long> subjectIdByCode = subjectRepository.findAll().stream()
                .collect(Collectors.toUnmodifiableMap(Subject::getCode, Subject::getId));

        if (subjectIdByCode.isEmpty()) {
            throw new IllegalStateException(
                    "No subjects found in database. Ensure Flyway V2 migration ran before seeding.");
        }

        transactionTemplate.executeWithoutResult(status -> {
            if (cleanExistingData) {
                log.info("Cleaning unverified/incomplete existing dataset before fresh CSV import...");
                jdbcTemplate.execute(
                        "TRUNCATE TABLE student_block_scores, exam_scores, students RESTART IDENTITY CASCADE");
            }
            markMetadataInProgress(csvPath.getFileName().toString());
        });

        int batchSize = Math.max(500, importerProperties.getBatchSize());
        log.info("Starting streaming CSV import from '{}' (batchSize={})...", csvPath.toAbsolutePath(), batchSize);

        long startTime = System.currentTimeMillis();
        long nextStudentId = 1L;
        long importedStudents = 0L;
        long importedScores = 0L;
        long failedRows = 0L;
        long sourceRowCount = 0L;
        long lineNumber = 0L;

        List<StudentBatchItem> studentBatch = new ArrayList<>(batchSize);
        List<ScoreBatchItem> scoreBatch = new ArrayList<>(batchSize * 6);

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        try (DigestInputStream digestStream = new DigestInputStream(Files.newInputStream(csvPath), sha256);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(digestStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                String msg = "CSV file '" + csvPath + "' is empty.";
                log.warn(msg);
                markMetadataFailed(msg);
                dataSeedState.markNotReadyEmpty(msg);
                return;
            }
            lineNumber++;

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                sourceRowCount++;

                try {
                    Optional<ParsedStudentRow> parsedOpt = csvRowParser.parseLine(line);
                    if (parsedOpt.isEmpty()) {
                        continue;
                    }

                    ParsedStudentRow row = parsedOpt.get();
                    long assignedStudentId = nextStudentId++;
                    String provinceCode = row.sbd().length() >= 2 ? row.sbd().substring(0, 2) : "01";

                    studentBatch.add(new StudentBatchItem(
                            assignedStudentId, row.sbd(), row.languageCode(), provinceCode));

                    for (Map.Entry<String, BigDecimal> scoreEntry : row.subjectScores().entrySet()) {
                        Long subjectId = subjectIdByCode.get(scoreEntry.getKey());
                        if (subjectId != null) {
                            scoreBatch.add(new ScoreBatchItem(assignedStudentId, subjectId, scoreEntry.getValue()));
                        }
                    }

                    if (studentBatch.size() >= batchSize) {
                        flushBatch(studentBatch, scoreBatch);
                        importedStudents += studentBatch.size();
                        importedScores += scoreBatch.size();
                        studentBatch.clear();
                        scoreBatch.clear();

                        if (importedStudents % 100_000 == 0) {
                            log.info(
                                    "CSV Import progress: {} students, {} scores inserted...",
                                    importedStudents,
                                    importedScores);
                        }
                    }
                } catch (Exception rowEx) {
                    failedRows++;
                    if (failedRows <= 10) {
                        log.warn("Skipping invalid CSV row at line {}: {}", lineNumber, rowEx.getMessage());
                    }
                }
            }

            if (!studentBatch.isEmpty()) {
                flushBatch(studentBatch, scoreBatch);
                importedStudents += studentBatch.size();
                importedScores += scoreBatch.size();
                studentBatch.clear();
                scoreBatch.clear();
            }
        }

        if (importedStudents == 0) {
            String msg = "No valid student records were imported from CSV file '" + csvPath + "'.";
            log.warn(msg);
            markMetadataFailed(msg);
            dataSeedState.markNotReadyEmpty(msg);
            return;
        }

        String checksumHex = HexFormat.of().formatHex(sha256.digest());

        synchronizeSequences();
        precomputeBlockScores();

        long dbStudents = queryCount("students");
        long dbScores = queryCount("exam_scores");
        long dbBlocks = queryCount("student_block_scores");

        if (dbStudents != importedStudents || dbScores != importedScores || dbBlocks <= 0) {
            throw new IllegalStateException(String.format(
                    "Post-import count mismatch: expected (students=%d, scores=%d, blocks>0) "
                            + "but database has (students=%d, scores=%d, blocks=%d).",
                    importedStudents, importedScores, dbStudents, dbScores, dbBlocks));
        }

        analyzeTablesSafely();
        markMetadataCompleted(
                csvPath.getFileName().toString(),
                checksumHex,
                sourceRowCount,
                dbStudents,
                dbScores,
                dbBlocks,
                failedRows);

        refreshAllCaches();
        dataSeedState.markReadyImported("Imported " + dbStudents + " students successfully.");

        long durationMs = System.currentTimeMillis() - startTime;
        log.info(
                "CSV Import completed in {} ms: importedStudents={}, importedScores={}, precomputedBlocks={}, failedRows={}, sha256={}",
                durationMs,
                dbStudents,
                dbScores,
                dbBlocks,
                failedRows,
                checksumHex);
    }

    private void flushBatch(List<StudentBatchItem> students, List<ScoreBatchItem> scores) {
        transactionTemplate.executeWithoutResult(status -> {
            jdbcTemplate.batchUpdate(INSERT_STUDENT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    StudentBatchItem item = students.get(i);
                    ps.setLong(1, item.id());
                    ps.setString(2, item.sbd());
                    ps.setString(3, item.languageCode());
                    ps.setString(4, item.provinceCode());
                }

                @Override
                public int getBatchSize() {
                    return students.size();
                }
            });

            if (!scores.isEmpty()) {
                jdbcTemplate.batchUpdate(INSERT_SCORE_SQL, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ScoreBatchItem item = scores.get(i);
                        ps.setLong(1, item.studentId());
                        ps.setLong(2, item.subjectId());
                        ps.setBigDecimal(3, item.score());
                    }

                    @Override
                    public int getBatchSize() {
                        return scores.size();
                    }
                });
            }
        });
    }

    private void precomputeBlockScores() {
        log.info("Precomputing student_block_scores (A00, B00, C00, A01, D01..D07)...");
        long start = System.currentTimeMillis();
        String sql = """
                WITH scores_pivot AS (
                    SELECT
                        es.student_id,
                        MAX(CASE WHEN sub.code = 'TOAN' THEN es.score END) AS toan,
                        MAX(CASE WHEN sub.code = 'NGU_VAN' THEN es.score END) AS van,
                        MAX(CASE WHEN sub.code = 'NGOAI_NGU' THEN es.score END) AS nn,
                        MAX(CASE WHEN sub.code = 'VAT_LI' THEN es.score END) AS ly,
                        MAX(CASE WHEN sub.code = 'HOA_HOC' THEN es.score END) AS hoa,
                        MAX(CASE WHEN sub.code = 'SINH_HOC' THEN es.score END) AS sinh,
                        MAX(CASE WHEN sub.code = 'LICH_SU' THEN es.score END) AS su,
                        MAX(CASE WHEN sub.code = 'DIA_LI' THEN es.score END) AS dia
                    FROM exam_scores es
                    JOIN subjects sub ON sub.id = es.subject_id
                    GROUP BY es.student_id
                ),
                student_scores AS (
                    SELECT
                        sp.student_id,
                        s.province_code,
                        s.language_code,
                        sp.toan,
                        sp.van,
                        sp.nn,
                        sp.ly,
                        sp.hoa,
                        sp.sinh,
                        sp.su,
                        sp.dia
                    FROM scores_pivot sp
                    JOIN students s ON s.id = sp.student_id
                )
                INSERT INTO student_block_scores (student_id, province_code, block_code, total_score)
                SELECT
                    s.student_id,
                    s.province_code,
                    b.block_code,
                    b.total_score
                FROM student_scores s
                CROSS JOIN LATERAL (
                    VALUES
                        ('A00', ROUND(s.toan + s.ly + s.hoa, 2), s.toan IS NOT NULL AND s.ly IS NOT NULL AND s.hoa IS NOT NULL),
                        ('B00', ROUND(s.toan + s.hoa + s.sinh, 2), s.toan IS NOT NULL AND s.hoa IS NOT NULL AND s.sinh IS NOT NULL),
                        ('C00', ROUND(s.van + s.su + s.dia, 2), s.van IS NOT NULL AND s.su IS NOT NULL AND s.dia IS NOT NULL),
                        ('A01', ROUND(s.toan + s.ly + s.nn, 2), s.toan IS NOT NULL AND s.ly IS NOT NULL AND s.nn IS NOT NULL AND s.language_code = 'N1'),
                        ('D0' || SUBSTRING(COALESCE(s.language_code, 'N1'), 2, 1), ROUND(s.toan + s.van + s.nn, 2), s.toan IS NOT NULL AND s.van IS NOT NULL AND s.nn IS NOT NULL AND s.language_code IN ('N1', 'N2', 'N3', 'N4', 'N5', 'N6', 'N7'))
                ) AS b(block_code, total_score, is_valid)
                WHERE b.is_valid
                ON CONFLICT (student_id, block_code) DO NOTHING
                """;
        int inserted = jdbcTemplate.update(sql);
        long duration = System.currentTimeMillis() - start;
        log.info("student_block_scores precomputation completed in {} ms (inserted {} rows).", duration, inserted);
    }

    private void synchronizeSequences() {
        jdbcTemplate.execute(
                "SELECT setval(pg_get_serial_sequence('students', 'id'), "
                        + "COALESCE((SELECT MAX(id) FROM students), 1))");
        jdbcTemplate.execute(
                "SELECT setval(pg_get_serial_sequence('exam_scores', 'id'), "
                        + "COALESCE((SELECT MAX(id) FROM exam_scores), 1))");
    }

    private void analyzeTablesSafely() {
        for (String table : List.of("students", "exam_scores", "student_block_scores")) {
            try {
                jdbcTemplate.execute("ANALYZE " + table);
            } catch (DataAccessException ex) {
                log.warn("Could not run ANALYZE on table '{}' (non-fatal): {}", table, ex.getMessage());
            }
        }
    }

    private void refreshAllCaches() {
        blockTotalCache.refresh();
        provinceCache.refresh();
        reportService.clearCaches();
    }

    private Optional<ImportMetadataRow> loadImportMetadata() {
        return jdbcTemplate.query(
                SELECT_METADATA_SQL,
                ps -> ps.setString(1, DATASET_KEY),
                rs -> {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new ImportMetadataRow(
                            rs.getString("status"),
                            rs.getLong("imported_students"),
                            rs.getLong("imported_scores"),
                            rs.getLong("precomputed_blocks")));
                });
    }

    private long queryCount(String tableName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
        return count != null ? count : 0L;
    }

    private void markMetadataInProgress(String sourceFile) {
        String sql = """
                INSERT INTO import_metadata (
                    dataset_key, status, source_file, source_checksum, source_row_count,
                    imported_students, imported_scores, precomputed_blocks, failed_rows,
                    verified_by, started_at, completed_at, error_message
                )
                VALUES (?, 'IN_PROGRESS', ?, NULL, NULL, 0, 0, 0, 0, NULL, NOW(), NULL, NULL)
                ON CONFLICT (dataset_key) DO UPDATE SET
                    status = 'IN_PROGRESS',
                    source_file = EXCLUDED.source_file,
                    source_checksum = NULL,
                    source_row_count = NULL,
                    imported_students = 0,
                    imported_scores = 0,
                    precomputed_blocks = 0,
                    failed_rows = 0,
                    verified_by = NULL,
                    started_at = NOW(),
                    completed_at = NULL,
                    error_message = NULL
                """;
        jdbcTemplate.update(sql, DATASET_KEY, sourceFile);
    }

    private void markMetadataCompleted(
            String sourceFile,
            String checksumHex,
            long sourceRowCount,
            long studentsCount,
            long scoresCount,
            long blocksCount,
            long failedRows) {
        String sql = """
                UPDATE import_metadata
                SET status = 'COMPLETED',
                    source_file = ?,
                    source_checksum = ?,
                    source_row_count = ?,
                    imported_students = ?,
                    imported_scores = ?,
                    precomputed_blocks = ?,
                    failed_rows = ?,
                    verified_by = 'CSV_IMPORT',
                    completed_at = NOW(),
                    error_message = NULL
                WHERE dataset_key = ?
                """;
        jdbcTemplate.update(
                sql,
                sourceFile,
                checksumHex,
                sourceRowCount,
                studentsCount,
                scoresCount,
                blocksCount,
                failedRows,
                DATASET_KEY);
    }

    private void markMetadataFailed(String errorMessage) {
        String sql = """
                INSERT INTO import_metadata (dataset_key, status, started_at, completed_at, error_message)
                VALUES (?, 'FAILED', NOW(), NULL, ?)
                ON CONFLICT (dataset_key) DO UPDATE SET
                    status = 'FAILED',
                    completed_at = NULL,
                    error_message = EXCLUDED.error_message
                """;
        jdbcTemplate.update(sql, DATASET_KEY, errorMessage);
    }

    private void markMetadataFailedSafely(String errorMessage) {
        try {
            markMetadataFailed(errorMessage);
        } catch (Exception ex) {
            log.warn("Unable to persist FAILED status to import_metadata: {}", ex.getMessage());
        }
    }

    private Optional<Path> resolveCsvPath(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            return Optional.empty();
        }
        String trimmed = configuredPath.trim();
        List<Path> candidates = "../dataset/diem_thi_thpt_2024.csv".equals(trimmed)
                ? List.of(
                        Paths.get(trimmed),
                        Paths.get("dataset/diem_thi_thpt_2024.csv"))
                : List.of(Paths.get(trimmed));

        for (Path candidate : candidates) {
            if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private record ImportMetadataRow(
            String status, long importedStudents, long importedScores, long precomputedBlocks) {}

    private record StudentBatchItem(long id, String sbd, String languageCode, String provinceCode) {}

    private record ScoreBatchItem(long studentId, long subjectId, BigDecimal score) {}
}
