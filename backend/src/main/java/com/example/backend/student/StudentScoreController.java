package com.example.backend.student;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Validated
public class StudentScoreController {

    private final StudentScoreService studentScoreService;

    @GetMapping("/{sbd}/scores")
    public ResponseEntity<StudentScoreResponse> getStudentScores(
            @PathVariable("sbd")
            @Pattern(
                    regexp = "^\\d{8}$",
                    message = "Số báo danh không hợp lệ. Vui lòng nhập đúng 8 chữ số (ví dụ: 01000001).")
            String sbd) {
        return ResponseEntity.ok(studentScoreService.getScoresBySbd(sbd));
    }
}

