package com.example.backend.subject;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LanguageCode {
    N1("N1", "Tiếng Anh"),
    N2("N2", "Tiếng Nga"),
    N3("N3", "Tiếng Pháp"),
    N4("N4", "Tiếng Trung"),
    N5("N5", "Tiếng Đức"),
    N6("N6", "Tiếng Nhật"),
    N7("N7", "Tiếng Hàn");

    private static final Map<String, LanguageCode> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(LanguageCode::getCode, Function.identity()));

    private final String code;
    private final String languageName;

    public String getDisplayLabel() {
        return code + " - " + languageName;
    }

    public static Optional<LanguageCode> fromCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_CODE.get(rawCode.trim().toUpperCase()));
    }
}

