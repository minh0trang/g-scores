package com.example.backend.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backend.common.GlobalExceptionHandler;
import com.example.backend.importer.DataSeedState;
import com.example.backend.province.ProvinceCache;
import com.example.backend.score.ExamScore;
import com.example.backend.score.ExamScoreRepository;
import com.example.backend.subject.Subject;
import com.example.backend.subject.SubjectRegistry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

class StudentScoreServiceReadinessTests {

    private StudentRepository studentRepository;
    private ExamScoreRepository examScoreRepository;
    private StudentBlockScoreRepository studentBlockScoreRepository;
    private BlockTotalCache blockTotalCache;
    private ProvinceCache provinceCache;
    private DataSeedState dataSeedState;
    private StudentScoreService studentScoreService;
    private MockMvc mockMvc;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        studentRepository = mock(StudentRepository.class);
        examScoreRepository = mock(ExamScoreRepository.class);
        studentBlockScoreRepository = mock(StudentBlockScoreRepository.class);
        blockTotalCache = mock(BlockTotalCache.class);
        provinceCache = mock(ProvinceCache.class);
        dataSeedState = new DataSeedState();

        studentScoreService = new StudentScoreService(
                studentRepository,
                examScoreRepository,
                studentBlockScoreRepository,
                blockTotalCache,
                provinceCache,
                new SubjectRegistry(),
                dataSeedState);

        mockMvc = MockMvcBuilders.standaloneSetup(new StudentScoreController(studentScoreService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void lookupShouldReturn503WhenDataNotReadyAcrossAllUnreadyStates() throws Exception {
        dataSeedState.markSeeding("Seeding in progress");
        mockMvc.perform(get("/api/v1/students/26020938/scores"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));

        dataSeedState.markNotReadyUnverified("Legacy unverified");
        mockMvc.perform(get("/api/v1/students/26020938/scores"))
                .andExpect(status().isServiceUnavailable());

        dataSeedState.markDataInconsistent("Mismatch");
        mockMvc.perform(get("/api/v1/students/26020938/scores"))
                .andExpect(status().isServiceUnavailable());

        verify(studentRepository, never()).findBySbd("26020938");
    }

    @Test
    void lookupShouldReturn503InsteadOfStaleDataOrFalse404WhenEpochChangesDuringQuery() throws Exception {
        dataSeedState.markReadyExisting("Ready epoch 1"); // epoch = 1

        CountDownLatch enteredTruncatedQuery = new CountDownLatch(1);
        CountDownLatch allowTruncatedFinish = new CountDownLatch(1);
        CountDownLatch enteredStaleQuery = new CountDownLatch(1);
        CountDownLatch allowStaleFinish = new CountDownLatch(1);
        AtomicInteger callCount = new AtomicInteger(0);

        Student freshStudent = Student.builder()
                .id(1L)
                .sbd("26020938")
                .provinceCode("26")
                .languageCode("N1")
                .build();

        when(studentRepository.findBySbd("26020938")).thenAnswer(inv -> {
            int call = callCount.incrementAndGet();
            if (call == 1) {
                enteredTruncatedQuery.countDown();
                allowTruncatedFinish.await(5, TimeUnit.SECONDS);
                return Optional.empty(); // Simulates truncated table mid-seeding
            }
            if (call == 2) {
                enteredStaleQuery.countDown();
                allowStaleFinish.await(5, TimeUnit.SECONDS);
                return Optional.of(freshStudent);
            }
            return Optional.of(freshStudent);
        });

        Subject math = Subject.builder().id(1L).code("TOAN").name("Toán").build();
        when(examScoreRepository.findByStudentIdWithSubject(1L))
                .thenReturn(List.of(ExamScore.builder().id(1L).student(freshStudent).subject(math).score(new BigDecimal("9.60")).build()));
        when(studentBlockScoreRepository.findBlockRanksByStudentId(1L))
                .thenReturn(List.of(blockProjection("A00", new BigDecimal("29.60"), 1L, 1L)));
        when(blockTotalCache.getNationalTotal("A00")).thenReturn(343800L);
        when(blockTotalCache.getProvincialTotal("A00", "26")).thenReturn(9064L);
        when(provinceCache.getProvinceName("26")).thenReturn("Thái Bình");

        // Case A: Table truncated mid-query -> MUST return 503, NEVER false 404
        Future<Throwable> truncatedFuture = executor.submit(() -> {
            try {
                mockMvc.perform(get("/api/v1/students/26020938/scores"))
                        .andExpect(status().isServiceUnavailable())
                        .andExpect(jsonPath("$.status").value(503));
                return null;
            } catch (Throwable t) {
                return t;
            }
        });

        assertEquals(true, enteredTruncatedQuery.await(5, TimeUnit.SECONDS));
        dataSeedState.markSeeding("Seeding epoch 2");
        dataSeedState.markReadyImported("Ready epoch 3");
        allowTruncatedFinish.countDown();

        Throwable truncatedErr = truncatedFuture.get(5, TimeUnit.SECONDS);
        if (truncatedErr != null) {
            throw new AssertionError("Expected 503 instead of false 404 during epoch transition", truncatedErr);
        }

        // Case B: Student row read at epoch 3, then epoch transitions again to 5 before exit gate -> MUST return 503
        Future<Throwable> staleFuture = executor.submit(() -> {
            try {
                mockMvc.perform(get("/api/v1/students/26020938/scores"))
                        .andExpect(status().isServiceUnavailable())
                        .andExpect(jsonPath("$.status").value(503));
                return null;
            } catch (Throwable t) {
                return t;
            }
        });

        assertEquals(true, enteredStaleQuery.await(5, TimeUnit.SECONDS));
        dataSeedState.markSeeding("Seeding epoch 4");
        dataSeedState.markReadyImported("Ready epoch 5");
        allowStaleFinish.countDown();

        Throwable staleErr = staleFuture.get(5, TimeUnit.SECONDS);
        if (staleErr != null) {
            throw new AssertionError("Expected 503 instead of stale data during epoch transition", staleErr);
        }

        // Case C: Fresh request on stable epoch 5 -> HTTP 200 with full contract
        mockMvc.perform(get("/api/v1/students/26020938/scores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sbd").value("26020938"))
                .andExpect(jsonPath("$.provinceCode").value("26"))
                .andExpect(jsonPath("$.provinceName").value("Thái Bình"))
                .andExpect(jsonPath("$.scores.TOAN").value(9.60))
                .andExpect(jsonPath("$.blocks[0].blockCode").value("A00"))
                .andExpect(jsonPath("$.blocks[0].nationalRank").value(1));
    }

    @Test
    void lookupShouldPreserve400ForInvalidFormatAnd404WhenReady() throws Exception {
        dataSeedState.markReadyExisting("Ready");
        when(studentRepository.findBySbd("99999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/students/123/scores"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/students/99999999/scores"))
                .andExpect(status().isNotFound());
    }

    private StudentBlockRankProjection blockProjection(
            String blockCode, BigDecimal totalScore, long natRank, long provRank) {
        return new StudentBlockRankProjection() {
            @Override
            public String getBlockCode() {
                return blockCode;
            }

            @Override
            public BigDecimal getTotalScore() {
                return totalScore;
            }

            @Override
            public Long getNationalRank() {
                return natRank;
            }

            @Override
            public Long getProvincialRank() {
                return provRank;
            }
        };
    }
}

