package com.example.backend.importer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.importer")
@Getter
@Setter
public class ImporterProperties {

    private boolean enabled = true;
    private String csvPath = "../dataset/diem_thi_thpt_2024.csv";
    private int batchSize = 5000;
}

