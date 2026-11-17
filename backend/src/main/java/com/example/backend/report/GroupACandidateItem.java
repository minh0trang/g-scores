package com.example.backend.report;

import java.math.BigDecimal;

public record GroupACandidateItem(
        int rank,
        String sbd,
        BigDecimal toan,
        BigDecimal vatLi,
        BigDecimal hoaHoc,
        BigDecimal totalScore,
        String languageCode) {
}

