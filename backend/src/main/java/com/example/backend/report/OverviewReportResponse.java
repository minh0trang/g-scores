package com.example.backend.report;

import java.math.BigDecimal;

public record OverviewReportResponse(
        long totalStudents,
        long totalScores,
        int totalSubjects,
        BigDecimal topGroupAScore) {
}

