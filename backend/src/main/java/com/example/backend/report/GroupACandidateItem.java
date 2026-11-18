package com.example.backend.report;

import java.math.BigDecimal;

public record GroupACandidateItem(
        int rank,
        String sbd,
        String provinceCode,
        String provinceName,
        BigDecimal toan,
        BigDecimal vatLi,
        BigDecimal hoaHoc,
        BigDecimal totalScore,
        String languageCode) {
}
