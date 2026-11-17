package com.example.backend.student;

import java.math.BigDecimal;
import java.util.Map;

public record StudentScoreResponse(
        String sbd,
        String languageCode,
        String languageDisplayName,
        Map<String, BigDecimal> scores) {
}

