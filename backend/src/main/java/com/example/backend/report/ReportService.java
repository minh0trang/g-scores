package com.example.backend.report;

import com.example.backend.common.ServiceUnavailableException;
import com.example.backend.importer.DataSeedState;
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
    private final DataSeedState dataSeedState;

    private final Map<String, CacheEntry<OverviewReportResponse>> cachedOverview = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<List<SubjectDistributionItem>>> cachedDistribution = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<List<GroupACandidateItem>>> cachedTop10GroupA = new ConcurrentHashMap<>();

    public void clearCaches() {
        cachedOverview.clear();
        cachedDistribution.clear();
        cachedTop10GroupA.clear();
    }

    private String normalizeKey(String provinceCode) {
        return (provinceCode == null || provinceCode.isBlank()) ? "ALL" : provinceCode.trim();
    }

    private DataSeedState.Snapshot requireDataReady() {
        DataSeedState.Snapshot snap = dataSeedState.snapshot();
        if (!snap.isDataReady()) {
            throw new ServiceUnavailableException(
                    "Dữ liệu điểm thi đang được khởi tạo hoặc chưa sẵn sàng ("
                            + snap.state().name()
                            + "). Vui lòng thử lại sau.");
        }
        return snap;
    }

    private void verifySnapshotStillCurrent(DataSeedState.Snapshot before) {
        DataSeedState.Snapshot after = dataSeedState.snapshot();
        if (!after.isDataReady() || after.epoch() != before.epoch()) {
            throw new ServiceUnavailableException(
                    "Dữ liệu điểm thi vừa được cập nhật trong lúc xử lý yêu cầu. Vui lòng thử lại.");
        }
    }

    @Transactional(readOnly = true)
    public OverviewReportResponse getOverviewStats(String provinceCode) {
        DataSeedState.Snapshot before = requireDataReady();

        String key = normalizeKey(provinceCode);
        CacheEntry<OverviewReportResponse> existing = cachedOverview.get(key);
        if (existing != null && existing.epoch() == before.epoch()) {
            verifySnapshotStillCurrent(before);
            return existing.value();
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

        verifySnapshotStillCurrent(before);

        if (totalStudents > 0) {
            cachedOverview.put(key, new CacheEntry<>(before.epoch(), response));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<SubjectDistributionItem> getScoreDistribution(String provinceCode) {
        DataSeedState.Snapshot before = requireDataReady();

        String key = normalizeKey(provinceCode);
        CacheEntry<List<SubjectDistributionItem>> existing = cachedDistribution.get(key);
        if (existing != null && existing.epoch() == before.epoch()) {
            verifySnapshotStillCurrent(before);
            return existing.value();
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
        long grandTotal = 0L;
        for (ExamSubject subject : subjectRegistry.getAllSubjects()) {
            SubjectDistributionProjection proj = byCode.get(subject.getCode());
            if (proj != null) {
                grandTotal += proj.getExcellent() + proj.getGood() + proj.getAverage() + proj.getLow();
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

        verifySnapshotStillCurrent(before);

        List<SubjectDistributionItem> immutableResult = List.copyOf(result);
        if (grandTotal > 0) {
            cachedDistribution.put(key, new CacheEntry<>(before.epoch(), immutableResult));
        }
        return immutableResult;
    }

    @Transactional(readOnly = true)
    public List<GroupACandidateItem> getTop10GroupA(String provinceCode) {
        DataSeedState.Snapshot before = requireDataReady();

        String key = normalizeKey(provinceCode);
        CacheEntry<List<GroupACandidateItem>> existing = cachedTop10GroupA.get(key);
        if (existing != null && existing.epoch() == before.epoch()) {
            verifySnapshotStillCurrent(before);
            return existing.value();
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

        verifySnapshotStillCurrent(before);

        List<GroupACandidateItem> immutableResult = List.copyOf(result);
        if (!immutableResult.isEmpty()) {
            cachedTop10GroupA.put(key, new CacheEntry<>(before.epoch(), immutableResult));
        }
        return immutableResult;
    }

    private record CacheEntry<T>(long epoch, T value) {}
}
