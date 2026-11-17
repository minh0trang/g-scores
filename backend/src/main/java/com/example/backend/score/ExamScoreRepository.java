package com.example.backend.score;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamScoreRepository extends JpaRepository<ExamScore, Long> {

    @Query("SELECT es FROM ExamScore es JOIN FETCH es.subject WHERE es.student.id = :studentId")
    List<ExamScore> findByStudentIdWithSubject(@Param("studentId") Long studentId);
}

