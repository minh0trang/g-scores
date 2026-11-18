package com.example.backend.province;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProvinceCache {

    private final ProvinceRepository provinceRepository;

    private volatile List<ProvinceResponse> cachedProvinces = Collections.emptyList();
    private final Map<String, String> provinceNameByCode = new ConcurrentHashMap<>();

    public synchronized void refresh() {
        try {
            List<Province> provinces = provinceRepository.findAllByOrderByCodeAsc();
            if (provinces.isEmpty()) {
                log.debug("ProvinceCache refresh called but provinces table is empty.");
                return;
            }

            List<ProvinceResponse> list = provinces.stream()
                    .map(p -> new ProvinceResponse(p.getCode(), p.getName()))
                    .toList();

            provinceNameByCode.clear();
            for (Province p : provinces) {
                provinceNameByCode.put(p.getCode(), p.getName());
            }

            this.cachedProvinces = list;
            log.info("ProvinceCache refreshed with {} provinces.", list.size());
        } catch (Exception ex) {
            log.warn("Failed to refresh ProvinceCache: {}", ex.getMessage());
        }
    }

    public List<ProvinceResponse> getAllProvinces() {
        if (cachedProvinces.isEmpty()) {
            refresh();
        }
        return cachedProvinces;
    }

    public String getProvinceName(String code) {
        if (code == null) {
            return "";
        }
        if (provinceNameByCode.isEmpty()) {
            refresh();
        }
        return provinceNameByCode.getOrDefault(code, "Mã tỉnh " + code);
    }

    public boolean isValidCode(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        if (provinceNameByCode.isEmpty()) {
            refresh();
        }
        return provinceNameByCode.containsKey(code);
    }
}

