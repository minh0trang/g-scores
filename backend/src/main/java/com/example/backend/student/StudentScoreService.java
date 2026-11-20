package com.example.backend.student;

import com.example.backend.common.BadRequestException;
import com.example.backend.common.ResourceNotFoundException;
import com.example.backend.common.ServiceUnavailableException;
import com.example.backend.importer.DataSeedState;
import com.example.backend.province.ProvinceCache;
import com.example.backend.score.ExamScore;
import com.example.backend.score.ExamScoreRepository;
import com.example.backend.subject.ExamSubject;
import com.example.backend.subject.ForeignLanguageSubject;
import com.example.backend.subject.SubjectRegistry;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentScoreService {

    private static final Pattern SBD_PATTERN = Pattern.compile("^\\d{8}$");

    private static final Map<String, String> BLOCK_NAMES = Map.ofEntries(
            Map.entry("A00", "Toán, Vật lí, Hóa học"),
            Map.entry("A01", "Toán, Vật lí, Tiếng Anh"),
            Map.entry("B00", "Toán, Hóa học, Sinh học"),
            Map.entry("C00", "Ngữ văn, Lịch sử, Địa lí"),
            Map.entry("D01", "Toán, Ngữ văn, Tiếng Anh"),
            Map.entry("D02", "Toán, Ngữ văn, Tiếng Nga"),
            Map.entry("D03", "Toán, Ngữ văn, Tiếng Pháp"),
            Map.entry("D04", "Toán, Ngữ văn, Tiếng Trung"),
            Map.entry("D05", "Toán, Ngữ văn, Tiếng Đức"),
            Map.entry("D06", "Toán, Ngữ văn, Tiếng Nhật"),
            Map.entry("D07", "Toán, Ngữ văn, Tiếng Hàn"));

    private final StudentRepository studentRepository;
    private final ExamScoreRepository examScoreRepository;
    private final StudentBlockScoreRepository studentBlockScoreRepository;
    private final BlockTotalCache blockTotalCache;
    private final ProvinceCache provinceCache;
    private final SubjectRegistry subjectRegistry;
    private final DataSeedState dataSeedState;

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
    public StudentScoreResponse getScoresBySbd(String rawSbd) {
        String normalizedSbd = rawSbd == null ? "" : rawSbd.trim();

        if (!SBD_PATTERN.matcher(normalizedSbd).matches()) {
            throw new BadRequestException(
                    "Số báo danh không hợp lệ. Vui lòng nhập đúng 8 chữ số (ví dụ: 01000001).");
        }

        DataSeedState.Snapshot before = requireDataReady();

        Optional<Student> studentOpt = studentRepository.findBySbd(normalizedSbd);
        if (studentOpt.isEmpty()) {
            verifySnapshotStillCurrent(before);
            throw new ResourceNotFoundException(
                    "Không tìm thấy dữ liệu điểm thi cho Số báo danh \"" + normalizedSbd + "\".");
        }
        Student student = studentOpt.get();

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

        // Single correlated query to compute ranks across all blocks for this student (no N+1)
        List<StudentBlockRankProjection> blockProjections =
                studentBlockScoreRepository.findBlockRanksByStudentId(student.getId());

        List<StudentBlockScoreResponse> blockResponses = new ArrayList<>(blockProjections.size());
        for (StudentBlockRankProjection proj : blockProjections) {
            String blockCode = proj.getBlockCode();
            String blockName = BLOCK_NAMES.getOrDefault(blockCode, blockCode);
            long nationalTotal = blockTotalCache.getNationalTotal(blockCode);
            long provincialTotal = blockTotalCache.getProvincialTotal(blockCode, student.getProvinceCode());

            blockResponses.add(new StudentBlockScoreResponse(
                    blockCode,
                    blockName,
                    proj.getTotalScore(),
                    proj.getNationalRank(),
                    nationalTotal,
                    proj.getProvincialRank(),
                    provincialTotal));
        }

        String provinceName = provinceCache.getProvinceName(student.getProvinceCode());

        verifySnapshotStillCurrent(before);

        return new StudentScoreResponse(
                student.getSbd(),
                student.getProvinceCode(),
                provinceName,
                student.getLanguageCode(),
                languageDisplayName,
                scoreByCode,
                blockResponses);
    }
}
