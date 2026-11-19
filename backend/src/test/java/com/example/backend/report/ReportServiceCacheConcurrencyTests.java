package com.example.backend.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backend.common.GlobalExceptionHandler;
import com.example.backend.common.ServiceUnavailableException;
import com.example.backend.importer.DataSeedState;
import com.example.backend.province.ProvinceCache;
import com.example.backend.score.ExamScoreRepository;
import com.example.backend.score.ExamScoreRepository.GroupATop10BlockProjection;
import com.example.backend.score.ExamScoreRepository.SubjectDistributionProjection;
import com.example.backend.student.StudentRepository;
import com.example.backend.subject.SubjectRegistry;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ReportServiceCacheConcurrencyTests {

    private StudentRepository studentRepository;
    private ExamScoreRepository examScoreRepository;
    private SubjectRegistry subjectRegistry;
    private DataSeedState dataSeedState;
    private ProvinceCache provinceCache;
    private ReportService reportService;
    private MockMvc mockMvc;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        studentRepository = mock(StudentRepository.class);
        examScoreRepository = mock(ExamScoreRepository.class);
        subjectRegistry = new SubjectRegistry();
        dataSeedState = new DataSeedState();
        provinceCache = mock(ProvinceCache.class);
        when(provinceCache.isValidCode("01")).thenReturn(true);

        reportService = new ReportService(studentRepository, examScoreRepository, subjectRegistry, dataSeedState);
        mockMvc = MockMvcBuilders.standaloneSetup(new ReportController(reportService, provinceCache))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void reportApisShouldReturn503WhenNotReadyOrDuringSeeding() throws Exception {
        dataSeedState.markSeeding("Importing 1M rows");

        mockMvc.perform(get("/api/v1/reports/overview"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));

        mockMvc.perform(get("/api/v1/reports/score-distribution"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));

        mockMvc.perform(get("/api/v1/reports/group-a/top-10"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));

        dataSeedState.markNotReadyUnverified("Legacy unverified");
        mockMvc.perform(get("/api/v1/reports/score-distribution"))
                .andExpect(status().isServiceUnavailable());

        dataSeedState.markDataInconsistent("Mismatch");
        mockMvc.perform(get("/api/v1/reports/group-a/top-10"))
                .andExpect(status().isServiceUnavailable());

        verify(examScoreRepository, never()).aggregateScoreDistributionBySubject();
        verify(examScoreRepository, never()).findTop10GroupAFromBlockScores(null);
    }

    @Test
    void scoreDistributionShouldReturn503AndNotCacheIfEpochChangesDuringQuery() throws Exception {
        dataSeedState.markReadyExisting("Ready epoch 1"); // epoch = 1

        CountDownLatch enteredQuery = new CountDownLatch(1);
        CountDownLatch allowQueryFinish = new CountDownLatch(1);
        AtomicInteger callCount = new AtomicInteger(0);

        SubjectDistributionProjection staleRow = distributionProjection("TOAN", 10L, 20L, 5L, 1L);
        SubjectDistributionProjection freshRow = distributionProjection("TOAN", 198392L, 505836L, 258654L, 82731L);

        when(examScoreRepository.aggregateScoreDistributionBySubject()).thenAnswer(inv -> {
            int call = callCount.incrementAndGet();
            if (call == 1) {
                enteredQuery.countDown();
                allowQueryFinish.await(5, TimeUnit.SECONDS);
                return List.of(staleRow);
            }
            return List.of(freshRow);
        });

        Future<Throwable> oldRequestFailure = executor.submit(() -> {
            try {
                mockMvc.perform(get("/api/v1/reports/score-distribution"))
                        .andExpect(status().isServiceUnavailable())
                        .andExpect(jsonPath("$.status").value(503));
                return null;
            } catch (Throwable t) {
                return t;
            }
        });

        assertEquals(true, enteredQuery.await(5, TimeUnit.SECONDS));

        // Transition epoch 1 -> epoch 2 (SEEDING) -> epoch 3 (READY_IMPORTED) while query #1 is in flight
        dataSeedState.markSeeding("Re-importing");
        reportService.clearCaches();
        dataSeedState.markReadyImported("Imported epoch 3");

        allowQueryFinish.countDown();
        Throwable failure = oldRequestFailure.get(5, TimeUnit.SECONDS);
        if (failure != null) {
            throw new AssertionError("Old request should have returned HTTP 503", failure);
        }

        // Fresh request at epoch 3 must hit DB (stale epoch 1 was NOT cached) and return fresh data
        mockMvc.perform(get("/api/v1/reports/score-distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subjectCode").value("TOAN"))
                .andExpect(jsonPath("$[0].excellent").value(198392));

        // Third request must hit epoch-3 cache
        mockMvc.perform(get("/api/v1/reports/score-distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].excellent").value(198392));

        verify(examScoreRepository, times(2)).aggregateScoreDistributionBySubject();
    }

    @Test
    void top10GroupAShouldReturn503AndNotCacheIfEpochChangesDuringQuery() throws Exception {
        dataSeedState.markReadyExisting("Ready epoch 1"); // epoch = 1

        CountDownLatch enteredQuery = new CountDownLatch(1);
        CountDownLatch allowQueryFinish = new CountDownLatch(1);
        AtomicInteger callCount = new AtomicInteger(0);

        GroupATop10BlockProjection staleCandidate = top10Projection(
                "01000001", "01", "Hà Nội", new BigDecimal("25.00"));
        GroupATop10BlockProjection freshCandidate = top10Projection(
                "26020938", "26", "Thái Bình", new BigDecimal("29.60"));

        when(examScoreRepository.findTop10GroupAFromBlockScores(null)).thenAnswer(inv -> {
            int call = callCount.incrementAndGet();
            if (call == 1) {
                enteredQuery.countDown();
                allowQueryFinish.await(5, TimeUnit.SECONDS);
                return List.of(staleCandidate);
            }
            return List.of(freshCandidate);
        });

        Future<Throwable> oldRequestFailure = executor.submit(() -> {
            try {
                mockMvc.perform(get("/api/v1/reports/group-a/top-10"))
                        .andExpect(status().isServiceUnavailable())
                        .andExpect(jsonPath("$.status").value(503));
                return null;
            } catch (Throwable t) {
                return t;
            }
        });

        assertEquals(true, enteredQuery.await(5, TimeUnit.SECONDS));

        dataSeedState.markSeeding("Re-importing");
        reportService.clearCaches();
        dataSeedState.markReadyImported("Imported epoch 3");

        allowQueryFinish.countDown();
        Throwable failure = oldRequestFailure.get(5, TimeUnit.SECONDS);
        if (failure != null) {
            throw new AssertionError("Old Top-10 request should have returned HTTP 503", failure);
        }

        mockMvc.perform(get("/api/v1/reports/group-a/top-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sbd").value("26020938"))
                .andExpect(jsonPath("$[0].provinceName").value("Thái Bình"));

        mockMvc.perform(get("/api/v1/reports/group-a/top-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sbd").value("26020938"));

        verify(examScoreRepository, times(2)).findTop10GroupAFromBlockScores(null);
    }

    @Test
    void overviewShouldThrow503IfEpochChangesDuringQuery() throws Exception {
        dataSeedState.markReadyExisting("Ready epoch 1");

        CountDownLatch enteredQuery = new CountDownLatch(1);
        CountDownLatch allowQueryFinish = new CountDownLatch(1);

        when(studentRepository.count()).thenAnswer(inv -> {
            enteredQuery.countDown();
            allowQueryFinish.await(5, TimeUnit.SECONDS);
            return 100L;
        });

        Future<Throwable> oldRequestFailure = executor.submit(() -> {
            try {
                assertThrows(ServiceUnavailableException.class, () -> reportService.getOverviewStats(null));
                return null;
            } catch (Throwable t) {
                return t;
            }
        });

        assertEquals(true, enteredQuery.await(5, TimeUnit.SECONDS));
        dataSeedState.markSeeding("Seeding");
        dataSeedState.markReadyImported("Epoch 3");
        allowQueryFinish.countDown();

        Throwable failure = oldRequestFailure.get(5, TimeUnit.SECONDS);
        if (failure != null) {
            throw new AssertionError("Overview request across epoch change should throw 503", failure);
        }
    }

    @Test
    void emptyResultsWhileReadyShouldNotBeCached() {
        dataSeedState.markReadyExisting("Ready");

        when(examScoreRepository.aggregateScoreDistributionBySubject()).thenReturn(Collections.emptyList());
        when(examScoreRepository.findTop10GroupAFromBlockScores(null)).thenReturn(Collections.emptyList());

        reportService.getScoreDistribution(null);
        reportService.getScoreDistribution(null);
        reportService.getTop10GroupA(null);
        reportService.getTop10GroupA(null);

        verify(examScoreRepository, times(2)).aggregateScoreDistributionBySubject();
        verify(examScoreRepository, times(2)).findTop10GroupAFromBlockScores(null);
    }

    private SubjectDistributionProjection distributionProjection(
            String code, long exc, long good, long avg, long low) {
        return new SubjectDistributionProjection() {
            @Override
            public String getSubjectCode() {
                return code;
            }

            @Override
            public long getExcellent() {
                return exc;
            }

            @Override
            public long getGood() {
                return good;
            }

            @Override
            public long getAverage() {
                return avg;
            }

            @Override
            public long getLow() {
                return low;
            }
        };
    }

    private GroupATop10BlockProjection top10Projection(
            String sbd, String provCode, String provName, BigDecimal total) {
        return new GroupATop10BlockProjection() {
            @Override
            public String getSbd() {
                return sbd;
            }

            @Override
            public String getProvinceCode() {
                return provCode;
            }

            @Override
            public String getProvinceName() {
                return provName;
            }

            @Override
            public BigDecimal getToan() {
                return new BigDecimal("9.60");
            }

            @Override
            public BigDecimal getVatLi() {
                return new BigDecimal("10.00");
            }

            @Override
            public BigDecimal getHoaHoc() {
                return new BigDecimal("10.00");
            }

            @Override
            public BigDecimal getTotalScore() {
                return total;
            }

            @Override
            public String getLanguageCode() {
                return "N1";
            }
        };
    }
}

