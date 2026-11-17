package com.example.backend.student;

import com.example.backend.common.BadRequestException;
import com.example.backend.common.ResourceNotFoundException;
import com.example.backend.score.ExamScore;
import com.example.backend.score.ExamScoreRepository;
import com.example.backend.subject.ExamSubject;
import com.example.backend.subject.ForeignLanguageSubject;
import com.example.backend.subject.SubjectRegistry;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentScoreService {

    private static final Pattern SBD_PATTERN = Pattern.compile("^\\d{8}$");

    private final StudentRepository studentRepository;
    private final ExamScoreRepository examScoreRepository;
    private final SubjectRegistry subjectRegistry;

    @Transactional(readOnly = true)
    public StudentScoreResponse getScoresBySbd(String rawSbd) {
        String normalizedSbd = rawSbd == null ? "" : rawSbd.trim();

        if (!SBD_PATTERN.matcher(normalizedSbd).matches()) {
            throw new BadRequestException(
                    "Số báo danh không hợp lệ. Vui lòng nhập đúng 8 chữ số (ví dụ: 01000001).");
        }

        Student student = studentRepository
                .findBySbd(normalizedSbd)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy dữ liệu điểm thi cho Số báo danh \"" + normalizedSbd + "\"."));

        List<ExamScore> takenScores = examScoreRepository.findByStudentIdWithSubject(student.getId());

        Map<String, BigDecimal> scoreByCode = new LinkedHashMap<>();
        for (ExamSubject subject : subjectRegistry.getAllSubjects()) {
            scoreByCode.put(subject.getCode(), null);
        }

        for (ExamScore examScore : takenScores) {
            scoreByCode.put(examScore.getSubject().getCode(), examScore.getScore());
        }

        ExamSubject foreignLangSubject = subjectRegistry.getRequiredByCode(ForeignLanguageSubject.CODE);
        String languageDisplayName = foreignLangSubject.formatDisplayName(student.getLanguageCode());

        return new StudentScoreResponse(
                student.getSbd(),
                student.getLanguageCode(),
                languageDisplayName,
                scoreByCode);
    }
}

