package com.example.backend.subject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SubjectRegistry {

    private final List<ExamSubject> orderedSubjects;
    private final Map<String, ExamSubject> byCode;
    private final Map<String, ExamSubject> byCsvColumn;

    public SubjectRegistry() {
        List<ExamSubject> subjects = List.of(
                new StandardExamSubject("TOAN", "Toán", "toan", 1),
                new StandardExamSubject("NGU_VAN", "Ngữ văn", "ngu_van", 2),
                new ForeignLanguageSubject(3),
                new StandardExamSubject("VAT_LI", "Vật lí", "vat_li", 4),
                new StandardExamSubject("HOA_HOC", "Hóa học", "hoa_hoc", 5),
                new StandardExamSubject("SINH_HOC", "Sinh học", "sinh_hoc", 6),
                new StandardExamSubject("LICH_SU", "Lịch sử", "lich_su", 7),
                new StandardExamSubject("DIA_LI", "Địa lí", "dia_li", 8),
                new StandardExamSubject("GDCD", "GDCD", "gdcd", 9));

        this.orderedSubjects = subjects;

        Map<String, ExamSubject> codeMap = new LinkedHashMap<>();
        Map<String, ExamSubject> csvMap = new LinkedHashMap<>();
        for (ExamSubject subject : subjects) {
            codeMap.put(subject.getCode(), subject);
            csvMap.put(subject.getCsvColumnName(), subject);
        }

        this.byCode = Collections.unmodifiableMap(codeMap);
        this.byCsvColumn = Collections.unmodifiableMap(csvMap);
    }

    public List<ExamSubject> getAllSubjects() {
        return orderedSubjects;
    }

    public Optional<ExamSubject> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byCode.get(code.trim().toUpperCase()));
    }

    public ExamSubject getRequiredByCode(String code) {
        return findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown subject code: " + code));
    }

    public Optional<ExamSubject> findByCsvColumn(String csvColumn) {
        if (csvColumn == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byCsvColumn.get(csvColumn.trim().toLowerCase()));
    }
}

