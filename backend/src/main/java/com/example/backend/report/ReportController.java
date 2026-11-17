package com.example.backend.report;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/overview")
    public ResponseEntity<OverviewReportResponse> getOverview() {
        return ResponseEntity.ok(reportService.getOverviewStats());
    }

    @GetMapping("/score-distribution")
    public ResponseEntity<List<SubjectDistributionItem>> getScoreDistribution() {
        return ResponseEntity.ok(reportService.getScoreDistribution());
    }

    @GetMapping("/group-a/top-10")
    public ResponseEntity<List<GroupACandidateItem>> getTop10GroupA() {
        return ResponseEntity.ok(reportService.getTop10GroupA());
    }
}

