package com.example.backend.subject;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScoreTier {
    EXCELLENT("Giỏi", ">= 8.0"),
    GOOD("Khá", "6.0 - <8.0"),
    AVERAGE("Trung bình", "4.0 - <6.0"),
    LOW("Yếu / Kém", "< 4.0");

    private static final BigDecimal TIER_EXCELLENT_MIN = new BigDecimal("8.00");
    private static final BigDecimal TIER_GOOD_MIN = new BigDecimal("6.00");
    private static final BigDecimal TIER_AVERAGE_MIN = new BigDecimal("4.00");

    private final String vietnameseLabel;
    private final String rangeDescription;

    public static ScoreTier fromScore(BigDecimal score) {
        if (score == null) {
            throw new IllegalArgumentException("Score must not be null when classifying tier");
        }
        if (score.compareTo(TIER_EXCELLENT_MIN) >= 0) {
            return EXCELLENT;
        }
        if (score.compareTo(TIER_GOOD_MIN) >= 0) {
            return GOOD;
        }
        if (score.compareTo(TIER_AVERAGE_MIN) >= 0) {
            return AVERAGE;
        }
        return LOW;
    }
}

