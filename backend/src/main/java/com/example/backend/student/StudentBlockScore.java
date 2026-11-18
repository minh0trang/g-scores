package com.example.backend.student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_block_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentBlockScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "province_code", length = 2, nullable = false)
    private String provinceCode;

    @Column(name = "block_code", length = 10, nullable = false)
    private String blockCode;

    @Column(name = "total_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal totalScore;
}

