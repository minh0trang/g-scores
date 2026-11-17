package com.example.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.backend.importer.CsvRowParser;
import com.example.backend.importer.ParsedStudentRow;
import com.example.backend.subject.ExamSubject;
import com.example.backend.subject.ForeignLanguageSubject;
import com.example.backend.subject.ScoreTier;
import com.example.backend.subject.SubjectRegistry;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BackendApplicationTests {

    private final SubjectRegistry subjectRegistry = new SubjectRegistry();
    private final CsvRowParser csvRowParser = new CsvRowParser(subjectRegistry);

    @Test
    void subjectRegistryShouldManageAllNineSubjectsAndFormatForeignLanguagePolymorphically() {
        assertEquals(9, subjectRegistry.getAllSubjects().size());

        ExamSubject foreignLang = subjectRegistry.getRequiredByCode(ForeignLanguageSubject.CODE);
        assertEquals("Ngoại ngữ (N1 - Tiếng Anh)", foreignLang.formatDisplayName("N1"));
        assertEquals("Ngoại ngữ (N6 - Tiếng Nhật)", foreignLang.formatDisplayName("N6"));
        assertEquals("Ngoại ngữ", foreignLang.formatDisplayName(null));
        assertEquals("Ngoại ngữ", foreignLang.formatDisplayName(""));

        ExamSubject math = subjectRegistry.getRequiredByCode("TOAN");
        assertEquals("Toán", math.formatDisplayName("N1"));
    }

    @Test
    void scoreTierShouldClassifyAllFourBoundariesAccurately() {
        assertEquals(ScoreTier.EXCELLENT, ScoreTier.fromScore(new BigDecimal("8.00")));
        assertEquals(ScoreTier.EXCELLENT, ScoreTier.fromScore(new BigDecimal("10.00")));
        assertEquals(ScoreTier.GOOD, ScoreTier.fromScore(new BigDecimal("7.99")));
        assertEquals(ScoreTier.GOOD, ScoreTier.fromScore(new BigDecimal("6.00")));
        assertEquals(ScoreTier.AVERAGE, ScoreTier.fromScore(new BigDecimal("5.99")));
        assertEquals(ScoreTier.AVERAGE, ScoreTier.fromScore(new BigDecimal("4.00")));
        assertEquals(ScoreTier.LOW, ScoreTier.fromScore(new BigDecimal("3.99")));
        assertEquals(ScoreTier.LOW, ScoreTier.fromScore(new BigDecimal("0.00")));
    }

    @Test
    void csvRowParserShouldPreserveLeadingZerosAndOmitUntakenSubjects() {
        String khtnRow = "01000001,8.4,6.75,8.0,6.0,5.25,5.0,,,,N1";
        Optional<ParsedStudentRow> parsedOpt = csvRowParser.parseLine(khtnRow);

        assertTrue(parsedOpt.isPresent());
        ParsedStudentRow row = parsedOpt.get();
        assertEquals("01000001", row.sbd());
        assertEquals("N1", row.languageCode());
        assertEquals(6, row.subjectScores().size());
        assertEquals(new BigDecimal("8.40"), row.subjectScores().get("TOAN"));
        assertFalse(row.subjectScores().containsKey("LICH_SU"));
        assertFalse(row.subjectScores().containsKey("DIA_LI"));
        assertFalse(row.subjectScores().containsKey("GDCD"));
    }

    @Test
    void csvRowParserShouldHandleTrailingEmptyLanguageCodeColumn() {
        String khxhExemptLangRow = "01000007,6.0,7.5,,,,,6.75,7.0,7.0,";
        Optional<ParsedStudentRow> parsedOpt = csvRowParser.parseLine(khxhExemptLangRow);

        assertTrue(parsedOpt.isPresent());
        ParsedStudentRow row = parsedOpt.get();
        assertEquals("01000007", row.sbd());
        assertNull(row.languageCode());
        assertEquals(5, row.subjectScores().size());
        assertFalse(row.subjectScores().containsKey("NGOAI_NGU"));
    }
}
