package com.example.backend.student;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlockTotalCache {

    private final StudentBlockScoreRepository studentBlockScoreRepository;

    private final Map<String, Long> nationalTotals = new ConcurrentHashMap<>();
    private final Map<String, Long> provincialTotals = new ConcurrentHashMap<>();

    public synchronized void refresh() {
        try {
            var natRows = studentBlockScoreRepository.aggregateNationalTotals();
            if (natRows.isEmpty()) {
                log.debug("BlockTotalCache: student_block_scores table is empty, skipping refresh.");
                return;
            }

            nationalTotals.clear();
            for (var row : natRows) {
                if (row.getBlockCode() != null && row.getTotalCount() != null) {
                    nationalTotals.put(row.getBlockCode(), row.getTotalCount());
                }
            }

            var provRows = studentBlockScoreRepository.aggregateProvincialTotals();
            provincialTotals.clear();
            for (var row : provRows) {
                if (row.getBlockCode() != null && row.getProvinceCode() != null && row.getTotalCount() != null) {
                    provincialTotals.put(row.getBlockCode() + ":" + row.getProvinceCode(), row.getTotalCount());
                }
            }

            log.info(
                    "BlockTotalCache refreshed: {} national blocks, {} provincial block totals.",
                    nationalTotals.size(),
                    provincialTotals.size());
        } catch (Exception ex) {
            log.warn("Failed to refresh BlockTotalCache: {}", ex.getMessage());
        }
    }

    public long getNationalTotal(String blockCode) {
        if (nationalTotals.isEmpty()) {
            refresh();
        }
        return nationalTotals.getOrDefault(blockCode, 0L);
    }

    public long getProvincialTotal(String blockCode, String provinceCode) {
        if (provincialTotals.isEmpty()) {
            refresh();
        }
        return provincialTotals.getOrDefault(blockCode + ":" + provinceCode, 0L);
    }
}

