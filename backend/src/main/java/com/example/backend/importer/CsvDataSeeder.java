package com.example.backend.importer;

import com.example.backend.province.ProvinceCache;
import com.example.backend.student.BlockTotalCache;
import com.example.backend.subject.Subject;
import com.example.backend.subject.SubjectRepository;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvDataSeeder implements ApplicationRunner {

    private static final String INSERT_STUDENT_SQL =
            "INSERT INTO students (id, sbd, language_code, province_code) VALUES (?, ?, ?, ?) ON CONFLICT (sbd) DO NOTHING";

    private static final String INSERT_SCORE_SQL =
            "INSERT INTO exam_scores (student_id, subject_id, score) VALUES (?, ?, ?) "
                    + "ON CONFLICT (student_id, subject_id) DO NOTHING";

    private final ImporterProperties importerProperties;
    private final CsvRowParser csvRowParser;
    private final SubjectRepository subjectRepository;
    private final JdbcTemplate jdbcTemplate;
    private final BlockTotalCache blockTotalCache;
    private final ProvinceCache provinceCache;

    @Override
    public void run(ApplicationArguments args) {
        if (!importerProperties.isEnabled()) {
            log.info("CSV Importer is disabled (app.importer.enabled=false). Skipping seed.");
            return;
        }

        Long existingStudents = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Long.class);
        if (existingStudents != null && existingStudents > 0) {
            log.info(
                    "Database already contains {} students. Checking block scores precomputation...",
                    existingStudents);
            ensureBlockScoresPrecomputed();
            blockTotalCache.refresh();
            provinceCache.refresh();
            return;
        }

        Optional<Path> csvPathOpt = resolveCsvPath(importerProperties.getCsvPath());
        if (csvPathOpt.isEmpty()) {
            log.warn(
                    "CSV dataset file not found at '{}'. Skipping initial data seed.",
                    importerProperties.getCsvPath());
            return;
        }

        Path csvPath = csvPathOpt.get();
        Map<String, Long> subjectIdByCode = subjectRepository.findAll().stream()
                .collect(Collectors.toUnmodifiableMap(Subject::getCode, Subject::getId));

        if (subjectIdByCode.isEmpty()) {
            log.error("No subjects found in database. Ensure Flyway V2 migration ran before seeding.");
            return;
        }

        int batchSize = Math.max(500, importerProperties.getBatchSize());
        log.info("Starting streaming CSV import from '{}' (batchSize={})...", csvPath.toAbsolutePath(), batchSize);

        long startTime = System.currentTimeMillis();
        long nextStudentId = 1L;
        long importedStudents = 0L;
        long importedScores = 0L;
        long failedRows = 0L;
        long lineNumber = 0L;

        List<StudentBatchItem> studentBatch = new ArrayList<>(batchSize);
        List<ScoreBatchItem> scoreBatch = new ArrayList<>(batchSize * 6);

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            // Skip CSV header row
            String header = reader.readLine();
            if (header == null) {
                log.warn("CSV file '{}' is empty.", csvPath);
                return;
            }
            lineNumber++;

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

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

            synchronizeSequences();
            ensureBlockScoresPrecomputed();
            blockTotalCache.refresh();
            provinceCache.refresh();

            long durationMs = System.currentTimeMillis() - startTime;
            log.info(
                    "CSV Import completed in {} ms: importedStudents={}, importedScores={}, failedRows={}",
                    durationMs,
                    importedStudents,
                    importedScores,
                    failedRows);
        } catch (Exception ex) {
            log.error("Fatal error during streaming CSV import at line {}: {}", lineNumber, ex.getMessage(), ex);
        }
    }

    private void flushBatch(List<StudentBatchItem> students, List<ScoreBatchItem> scores) {
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
    }

    private void ensureBlockScoresPrecomputed() {
        try {
            Long blockCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM student_block_scores", Long.class);
            if (blockCount != null && blockCount > 0) {
                log.info("student_block_scores already populated ({} rows). Skipping precomputation.", blockCount);
                return;
            }

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
        } catch (Exception ex) {
            log.error("Failed to ensure block scores precomputation: {}", ex.getMessage(), ex);
        }
    }

    private void synchronizeSequences() {
        jdbcTemplate.execute(
                "SELECT setval(pg_get_serial_sequence('students', 'id'), "
                        + "COALESCE((SELECT MAX(id) FROM students), 1))");
        jdbcTemplate.execute(
                "SELECT setval(pg_get_serial_sequence('exam_scores', 'id'), "
                        + "COALESCE((SELECT MAX(id) FROM exam_scores), 1))");
    }

    private Optional<Path> resolveCsvPath(String configuredPath) {
        List<Path> candidates = List.of(
                Paths.get(configuredPath),
                Paths.get("../dataset/diem_thi_thpt_2024.csv"),
                Paths.get("dataset/diem_thi_thpt_2024.csv"));

        for (Path candidate : candidates) {
            if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private record StudentBatchItem(long id, String sbd, String languageCode, String provinceCode) {}

    private record ScoreBatchItem(long studentId, long subjectId, BigDecimal score) {}
}

