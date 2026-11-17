package com.example.backend.importer;

import com.example.backend.subject.ExamSubject;
import com.example.backend.subject.SubjectRegistry;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CsvRowParser {

    private static final int EXPECTED_COLUMN_COUNT = 11;
    private static final int SBD_COLUMN_INDEX = 0;
    private static final int LANGUAGE_CODE_COLUMN_INDEX = 10;

    private static final List<String> CSV_SUBJECT_COLUMNS = List.of(
            "toan",
            "ngu_van",
            "ngoai_ngu",
            "vat_li",
            "hoa_hoc",
            "sinh_hoc",
            "lich_su",
            "dia_li",
            "gdcd");

    private final SubjectRegistry subjectRegistry;

    public Optional<ParsedStudentRow> parseLine(String rawLine) {
        if (rawLine == null || rawLine.isBlank()) {
            return Optional.empty();
        }

        // Dùng limit = -1 để giữ nguyên các cột trống ở cuối dòng (ví dụ ma_ngoai_ngu rỗng)
        String[] tokens = rawLine.split(",", -1);
        if (tokens.length < EXPECTED_COLUMN_COUNT) {
            throw new IllegalArgumentException(
                    "Malformed CSV row (expected 11 columns, got " + tokens.length + "): " + rawLine);
        }

        String sbd = tokens[SBD_COLUMN_INDEX].trim();
        if (sbd.isEmpty()) {
            throw new IllegalArgumentException("Missing SBD in CSV row: " + rawLine);
        }

        Map<String, BigDecimal> subjectScores = new LinkedHashMap<>();

        for (int i = 0; i < CSV_SUBJECT_COLUMNS.size(); i++) {
            String csvColName = CSV_SUBJECT_COLUMNS.get(i);
            String cellValue = tokens[i + 1].trim();

            // Sparse Score Storage (Invariant #3): Ô trống = không thi môn đó, bỏ qua hoàn toàn
            if (cellValue.isEmpty()) {
                continue;
            }

            ExamSubject examSubject = subjectRegistry
                    .findByCsvColumn(csvColName)
                    .orElseThrow(() -> new IllegalStateException("Unmapped CSV column: " + csvColName));

            BigDecimal parsedScore = new BigDecimal(cellValue);
            BigDecimal normalizedScore = examSubject.normalizeScore(parsedScore);
            subjectScores.put(examSubject.getCode(), normalizedScore);
        }

        String rawLanguageCode = tokens[LANGUAGE_CODE_COLUMN_INDEX].trim();
        String languageCode = rawLanguageCode.isEmpty() ? null : rawLanguageCode.toUpperCase();

        return Optional.of(new ParsedStudentRow(sbd, languageCode, subjectScores));
    }
}

