package com.example.backend.province;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/provinces")
@RequiredArgsConstructor
public class ProvinceController {

    private final ProvinceCache provinceCache;

    @GetMapping
    public ResponseEntity<List<ProvinceResponse>> getProvinces() {
        return ResponseEntity.ok(provinceCache.getAllProvinces());
    }
}
