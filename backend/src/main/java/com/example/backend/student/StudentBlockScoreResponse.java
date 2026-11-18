package com.example.backend.student;

import java.math.BigDecimal;

public record StudentBlockScoreResponse(
        String blockCode,
        String blockName,
        BigDecimal totalScore,
        Long nationalRank,
        Long nationalTotal,
        Long provincialRank,
        Long provincialTotal) {
}
