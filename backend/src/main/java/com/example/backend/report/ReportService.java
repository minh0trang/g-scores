package com.example.backend.report;

import com.example.backend.score.ExamScoreRepository;
import com.example.backend.score.ExamScoreRepository.GroupATop10BlockProjection;
import com.example.backend.score.ExamScoreRepository.SubjectDistributionProjection;
import com.example.backend.student.StudentRepository;
import com.example.backend.subject.ExamSubject;
import com.example.backend.subject.SubjectRegistry;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StudentRepository studentRepository;
    private final ExamScoreRepository examScoreRepository;
    private final SubjectRegistry subjectRegistry;

    private final Map<String, OverviewReportResponse> cachedOverview = new ConcurrentHashMap<>();
    private final Map<String, List<SubjectDistributionItem>> cachedDistribution = new ConcurrentHashMap<>();
    private final Map<String, List<GroupACandidateItem>> cachedTop10GroupA = new ConcurrentHashMap<>();

    private String normalizeKey(String provinceCode) {
        return (provinceCode == null || provinceCode.isBlank()) ? "ALL" : provinceCode.trim();
    }

    @Transactional(readOnly = true)
    public OverviewReportResponse getOverviewStats(String provinceCode) {
        String key = normalizeKey(provinceCode);
        OverviewReportResponse existing = cachedOverview.get(key);
        if (existing != null) {
            return existing;
        }

        long totalStudents;
        long totalScores;
        int totalSubjects = subjectRegistry.getAllSubjects().size();

        if ("ALL".equals(key)) {
            totalStudents = studentRepository.count();
            totalScores = examScoreRepository.count();
        } else {
            totalStudents = studentRepository.countByProvinceCode(key);
            totalScores = examScoreRepository.countByProvinceCode(key);
        }

        List<GroupACandidateItem> topCandidates = getTop10GroupA(provinceCode);
        BigDecimal topGroupAScore = topCandidates.isEmpty()
                ? BigDecimal.ZERO
                : topCandidates.get(0).totalScore();

        OverviewReportResponse response =
                new OverviewReportResponse(totalStudents, totalScores, totalSubjects, topGroupAScore);

        if (totalStudents > 0) {
            cachedOverview.put(key, response);
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<SubjectDistributionItem> getScoreDistribution(String provinceCode) {
        String key = normalizeKey(provinceCode);
        List<SubjectDistributionItem> existing = cachedDistribution.get(key);
        if (existing != null) {
            return existing;
        }

        List<SubjectDistributionProjection> rows;
        if ("ALL".equals(key)) {
            rows = examScoreRepository.aggregateScoreDistributionBySubject();
        } else {
            rows = examScoreRepository.aggregateScoreDistributionBySubjectAndProvince(key);
        }

        Map<String, SubjectDistributionProjection> byCode = rows.stream()
                .collect(Collectors.toMap(SubjectDistributionProjection::getSubjectCode, Function.identity()));

        List<SubjectDistributionItem> result = new ArrayList<>();
        for (ExamSubject subject : subjectRegistry.getAllSubjects()) {
            SubjectDistributionProjection proj = byCode.get(subject.getCode());
            if (proj != null) {
                result.add(new SubjectDistributionItem(
                        subject.getCode(),
                        subject.getName(),
                        proj.getExcellent(),
                        proj.getGood(),
                        proj.getAverage(),
                        proj.getLow()));
            } else {
                result.add(new SubjectDistributionItem(subject.getCode(), subject.getName(), 0L, 0L, 0L, 0L));
            }
        }

        List<SubjectDistributionItem> immutableResult = List.copyOf(result);
        cachedDistribution.put(key, immutableResult);
        return immutableResult;
    }

    @Transactional(readOnly = true)
    public List<GroupACandidateItem> getTop10GroupA(String provinceCode) {
        String key = normalizeKey(provinceCode);
        List<GroupACandidateItem> existing = cachedTop10GroupA.get(key);
        if (existing != null) {
            return existing;
        }

        String filterProvince = "ALL".equals(key) ? null : key;
        List<GroupATop10BlockProjection> rows =
                examScoreRepository.findTop10GroupAFromBlockScores(filterProvince);
        List<GroupACandidateItem> result = new ArrayList<>(rows.size());

        int currentRank = 1;
        BigDecimal previousTotalScore = null;

        for (int i = 0; i < rows.size(); i++) {
            GroupATop10BlockProjection row = rows.get(i);
            BigDecimal currentTotal = row.getTotalScore();

            // Competition rank (1, 2, 2, 4)
            if (previousTotalScore != null && currentTotal.compareTo(previousTotalScore) != 0) {
                currentRank = i + 1;
            }
            previousTotalScore = currentTotal;

            result.add(new GroupACandidateItem(
                    currentRank,
                    row.getSbd(),
                    row.getProvinceCode(),
                    row.getProvinceName(),
                    row.getToan(),
                    row.getVatLi(),
                    row.getHoaHoc(),
                    currentTotal,
                    row.getLanguageCode()));
        }

        List<GroupACandidateItem> immutableResult = List.copyOf(result);
        cachedTop10GroupA.put(key, immutableResult);
        return immutableResult;
    }
}
