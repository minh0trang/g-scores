package com.example.backend.student;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentBlockScoreRepository extends JpaRepository<StudentBlockScore, Long> {

    @Query(value = """
            SELECT 
                my_blocks.block_code AS blockCode,
                my_blocks.total_score AS totalScore,
                (
                    SELECT COUNT(*) + 1 
                    FROM student_block_scores nat 
                    WHERE nat.block_code = my_blocks.block_code 
                      AND nat.total_score > my_blocks.total_score
                ) AS nationalRank,
                (
                    SELECT COUNT(*) + 1 
                    FROM student_block_scores prov 
                    WHERE prov.block_code = my_blocks.block_code 
                      AND prov.province_code = my_blocks.province_code 
                      AND prov.total_score > my_blocks.total_score
                ) AS provincialRank
            FROM student_block_scores my_blocks
            WHERE my_blocks.student_id = :studentId
            ORDER BY my_blocks.block_code
            """, nativeQuery = true)
    List<StudentBlockRankProjection> findBlockRanksByStudentId(@Param("studentId") Long studentId);

    @Query(value = "SELECT block_code AS blockCode, COUNT(*) AS totalCount FROM student_block_scores GROUP BY block_code", nativeQuery = true)
    List<BlockNationalTotalProjection> aggregateNationalTotals();

    @Query(value = "SELECT block_code AS blockCode, province_code AS provinceCode, COUNT(*) AS totalCount FROM student_block_scores GROUP BY block_code, province_code", nativeQuery = true)
    List<BlockProvincialTotalProjection> aggregateProvincialTotals();

    interface BlockNationalTotalProjection {
        String getBlockCode();
        Long getTotalCount();
    }

    interface BlockProvincialTotalProjection {
        String getBlockCode();
        String getProvinceCode();
        Long getTotalCount();
    }
}
