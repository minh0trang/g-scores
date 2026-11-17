package com.example.backend.subject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class ExamSubject {

    private static final BigDecimal MIN_SCORE = BigDecimal.ZERO;
    private static final BigDecimal MAX_SCORE = new BigDecimal("10.00");

    private final String code;
    private final String name;
    private final String csvColumnName;
    private final int displayOrder;

    public boolean isValidScore(BigDecimal score) {
        return score != null
                && score.compareTo(MIN_SCORE) >= 0
                && score.compareTo(MAX_SCORE) <= 0;
    }

    public BigDecimal normalizeScore(BigDecimal rawScore) {
        if (!isValidScore(rawScore)) {
            throw new IllegalArgumentException(
                    "Invalid score " + rawScore + " for subject " + code);
        }
        return rawScore.setScale(2, RoundingMode.HALF_UP);
    }

    public ScoreTier classifyTier(BigDecimal score) {
        return ScoreTier.fromScore(score);
    }

    /**
     * Đa hình hiển thị tên môn học dựa theo language_code của thí sinh.
     */
    public String formatDisplayName(String studentLanguageCode) {
        return name;
    }
}

