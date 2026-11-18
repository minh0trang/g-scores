package com.example.backend.student;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record StudentScoreResponse(
        String sbd,
        String provinceCode,
        String provinceName,
        String languageCode,
        String languageDisplayName,
        Map<String, BigDecimal> scores,
        List<StudentBlockScoreResponse> blocks) {
}
