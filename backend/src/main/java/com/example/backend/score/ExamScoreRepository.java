package com.example.backend.score;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamScoreRepository extends JpaRepository<ExamScore, Long> {

    @Query("SELECT es FROM ExamScore es JOIN FETCH es.subject WHERE es.student.id = :studentId")
    List<ExamScore> findByStudentIdWithSubject(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(es) FROM ExamScore es WHERE es.student.provinceCode = :provinceCode")
    long countByProvinceCode(@Param("provinceCode") String provinceCode);

    @Query(
            value = """
                    SELECT s.code AS subjectCode,
                           COALESCE(SUM(CASE WHEN es.score >= 8.00 THEN 1 ELSE 0 END), 0) AS excellent,
                           COALESCE(SUM(CASE WHEN es.score >= 6.00 AND es.score < 8.00 THEN 1 ELSE 0 END), 0) AS good,
                           COALESCE(SUM(CASE WHEN es.score >= 4.00 AND es.score < 6.00 THEN 1 ELSE 0 END), 0) AS average,
                           COALESCE(SUM(CASE WHEN es.score < 4.00 THEN 1 ELSE 0 END), 0) AS low
                    FROM subjects s
                    LEFT JOIN exam_scores es ON es.subject_id = s.id
                    GROUP BY s.code
                    """,
            nativeQuery = true)
    List<SubjectDistributionProjection> aggregateScoreDistributionBySubject();

    @Query(
            value = """
                    SELECT s.code AS subjectCode,
                           COALESCE(SUM(CASE WHEN es.score >= 8.00 THEN 1 ELSE 0 END), 0) AS excellent,
                           COALESCE(SUM(CASE WHEN es.score >= 6.00 AND es.score < 8.00 THEN 1 ELSE 0 END), 0) AS good,
                           COALESCE(SUM(CASE WHEN es.score >= 4.00 AND es.score < 6.00 THEN 1 ELSE 0 END), 0) AS average,
                           COALESCE(SUM(CASE WHEN es.score < 4.00 THEN 1 ELSE 0 END), 0) AS low
                    FROM subjects s
                    JOIN exam_scores es ON es.subject_id = s.id
                    JOIN students st ON st.id = es.student_id
                    WHERE st.province_code = :provinceCode
                    GROUP BY s.code
                    """,
            nativeQuery = true)
    List<SubjectDistributionProjection> aggregateScoreDistributionBySubjectAndProvince(
            @Param("provinceCode") String provinceCode);

    @Query(
            value = """
                    SELECT 
                        s.sbd AS sbd,
                        sbs.province_code AS provinceCode,
                        p.name AS provinceName,
                        sbs.total_score AS totalScore,
                        m.score AS toan,
                        v.score AS vatLi,
                        c.score AS hoaHoc,
                        s.language_code AS languageCode
                    FROM student_block_scores sbs
                    JOIN students s ON s.id = sbs.student_id
                    JOIN provinces p ON p.code = sbs.province_code
                    LEFT JOIN exam_scores m ON m.student_id = s.id AND m.subject_id = 1
                    LEFT JOIN exam_scores v ON v.student_id = s.id AND v.subject_id = 4
                    LEFT JOIN exam_scores c ON c.student_id = s.id AND c.subject_id = 5
                    WHERE sbs.block_code = 'A00' 
                      AND (:provinceCode IS NULL OR sbs.province_code = :provinceCode)
                    ORDER BY sbs.total_score DESC, s.sbd ASC
                    LIMIT 10
                    """,
            nativeQuery = true)
    List<GroupATop10BlockProjection> findTop10GroupAFromBlockScores(@Param("provinceCode") String provinceCode);

    interface SubjectDistributionProjection {
        String getSubjectCode();

        long getExcellent();

        long getGood();

        long getAverage();

        long getLow();
    }

    interface GroupATop10BlockProjection {
        String getSbd();

        String getProvinceCode();

        String getProvinceName();

        BigDecimal getToan();

        BigDecimal getVatLi();

        BigDecimal getHoaHoc();

        BigDecimal getTotalScore();

        String getLanguageCode();
    }
}
