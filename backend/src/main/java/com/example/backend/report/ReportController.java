package com.example.backend.report;

import com.example.backend.common.BadRequestException;
import com.example.backend.province.ProvinceCache;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ProvinceCache provinceCache;

    @GetMapping("/overview")
    public ResponseEntity<OverviewReportResponse> getOverview(
            @RequestParam(required = false) String province) {
        validateProvince(province);
        return ResponseEntity.ok(reportService.getOverviewStats(normalizeProvince(province)));
    }

    @GetMapping("/score-distribution")
    public ResponseEntity<List<SubjectDistributionItem>> getScoreDistribution(
            @RequestParam(required = false) String province) {
        validateProvince(province);
        return ResponseEntity.ok(reportService.getScoreDistribution(normalizeProvince(province)));
    }

    @GetMapping("/group-a/top-10")
    public ResponseEntity<List<GroupACandidateItem>> getTop10GroupA(
            @RequestParam(required = false) String province) {
        validateProvince(province);
        return ResponseEntity.ok(reportService.getTop10GroupA(normalizeProvince(province)));
    }

    private void validateProvince(String province) {
        if (province != null && !province.isBlank()) {
            String trimmed = province.trim();
            if (!provinceCache.isValidCode(trimmed)) {
                throw new BadRequestException("Mã tỉnh thành không hợp lệ: \"" + province + "\".");
            }
        }
    }

    private String normalizeProvince(String province) {
        if (province == null || province.isBlank()) {
            return null;
        }
        return province.trim();
    }
}
