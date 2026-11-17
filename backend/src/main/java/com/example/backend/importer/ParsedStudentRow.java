package com.example.backend.importer;

import java.math.BigDecimal;
import java.util.Map;

public record ParsedStudentRow(
        String sbd,
        String languageCode,
        Map<String, BigDecimal> subjectScores) {
}

