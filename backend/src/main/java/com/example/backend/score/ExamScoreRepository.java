package com.example.backend.score;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamScoreRepository extends JpaRepository<ExamScore, Long> {

    @Query("SELECT es FROM ExamScore es JOIN FETCH es.subject WHERE es.student.id = :studentId")
    List<ExamScore> findByStudentIdWithSubject(@Param("studentId") Long studentId);

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
                    SELECT st.sbd AS sbd,
                           st.language_code AS languageCode,
                           es_toan.score AS toan,
                           es_li.score AS vatLi,
                           es_hoa.score AS hoaHoc,
                           (es_toan.score + es_li.score + es_hoa.score) AS totalScore
                    FROM students st
                    JOIN exam_scores es_toan ON es_toan.student_id = st.id
                    JOIN subjects s_toan ON s_toan.id = es_toan.subject_id AND s_toan.code = 'TOAN'
                    JOIN exam_scores es_li ON es_li.student_id = st.id
                    JOIN subjects s_li ON s_li.id = es_li.subject_id AND s_li.code = 'VAT_LI'
                    JOIN exam_scores es_hoa ON es_hoa.student_id = st.id
                    JOIN subjects s_hoa ON s_hoa.id = es_hoa.subject_id AND s_hoa.code = 'HOA_HOC'
                    ORDER BY totalScore DESC, st.sbd ASC
                    LIMIT 10
                    """,
            nativeQuery = true)
    List<GroupACandidateProjection> findTop10GroupACandidates();

    interface SubjectDistributionProjection {
        String getSubjectCode();

        long getExcellent();

        long getGood();

        long getAverage();

        long getLow();
    }

    interface GroupACandidateProjection {
        String getSbd();

        String getLanguageCode();

        BigDecimal getToan();

        BigDecimal getVatLi();

        BigDecimal getHoaHoc();

        BigDecimal getTotalScore();
    }
}
