package com.example.backend.student;

import java.math.BigDecimal;

public interface StudentBlockRankProjection {
    String getBlockCode();
    BigDecimal getTotalScore();
    Long getNationalRank();
    Long getProvincialRank();
}
