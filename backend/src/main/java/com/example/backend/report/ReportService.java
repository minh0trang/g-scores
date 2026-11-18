package com.example.backend.report;

import com.example.backend.score.ExamScoreRepository;
import com.example.backend.score.ExamScoreRepository.GroupACandidateProjection;
import com.example.backend.score.ExamScoreRepository.SubjectDistributionProjection;
import com.example.backend.student.StudentRepository;
import com.example.backend.subject.ExamSubject;
import com.example.backend.subject.SubjectRegistry;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private volatile long cachedStudentCount = -1L;
    private volatile OverviewReportResponse cachedOverview;
    private volatile List<SubjectDistributionItem> cachedDistribution;
    private volatile List<GroupACandidateItem> cachedTop10GroupA;

    @Transactional(readOnly = true)
    public OverviewReportResponse getOverviewStats() {
        long currentStudentCount = studentRepository.count();
        if (currentStudentCount > 0 && currentStudentCount == cachedStudentCount && cachedOverview != null) {
            return cachedOverview;
        }

        long totalScores = examScoreRepository.count();
        int totalSubjects = subjectRegistry.getAllSubjects().size();

        List<GroupACandidateItem> topCandidates = getTop10GroupA();
        BigDecimal topGroupAScore = topCandidates.isEmpty()
                ? BigDecimal.ZERO
                : topCandidates.get(0).totalScore();

        OverviewReportResponse response =
                new OverviewReportResponse(currentStudentCount, totalScores, totalSubjects, topGroupAScore);

        if (currentStudentCount > 0) {
            this.cachedOverview = response;
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<SubjectDistributionItem> getScoreDistribution() {
        long currentStudentCount = studentRepository.count();
        if (currentStudentCount > 0 && currentStudentCount == cachedStudentCount && cachedDistribution != null) {
            return cachedDistribution;
        }

        Map<String, SubjectDistributionProjection> byCode = examScoreRepository
                .aggregateScoreDistributionBySubject()
                .stream()
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
        if (currentStudentCount > 0) {
            this.cachedDistribution = immutableResult;
            this.cachedStudentCount = currentStudentCount;
        }
        return immutableResult;
    }

    @Transactional(readOnly = true)
    public List<GroupACandidateItem> getTop10GroupA() {
        long currentStudentCount = studentRepository.count();
        if (currentStudentCount > 0 && currentStudentCount == cachedStudentCount && cachedTop10GroupA != null) {
            return cachedTop10GroupA;
        }

        List<GroupACandidateProjection> rows = examScoreRepository.findTop10GroupACandidates();
        List<GroupACandidateItem> result = new ArrayList<>(rows.size());

        int currentRank = 0;
        BigDecimal previousTotalScore = null;

        for (GroupACandidateProjection row : rows) {
            BigDecimal currentTotal = row.getTotalScore();
            if (previousTotalScore == null || currentTotal.compareTo(previousTotalScore) != 0) {
                currentRank++;
                previousTotalScore = currentTotal;
            }

            result.add(new GroupACandidateItem(
                    currentRank,
                    row.getSbd(),
                    row.getToan(),
                    row.getVatLi(),
                    row.getHoaHoc(),
                    currentTotal,
                    row.getLanguageCode()));
        }

        List<GroupACandidateItem> immutableResult = List.copyOf(result);
        if (currentStudentCount > 0) {
            this.cachedTop10GroupA = immutableResult;
            this.cachedStudentCount = currentStudentCount;
        }
        return immutableResult;
    }
}
