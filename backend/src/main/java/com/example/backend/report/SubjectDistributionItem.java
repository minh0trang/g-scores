package com.example.backend.report;

public record SubjectDistributionItem(
        String subjectCode,
        String subjectName,
        long excellent,
        long good,
        long average,
        long low) {
}

