package com.example.backend.subject;

public class ForeignLanguageSubject extends ExamSubject {

    public static final String CODE = "NGOAI_NGU";
    public static final String DEFAULT_NAME = "Ngoại ngữ";
    public static final String CSV_COLUMN = "ngoai_ngu";

    public ForeignLanguageSubject(int displayOrder) {
        super(CODE, DEFAULT_NAME, CSV_COLUMN, displayOrder);
    }

    @Override
    public String formatDisplayName(String studentLanguageCode) {
        return LanguageCode.fromCode(studentLanguageCode)
                .map(lang -> getName() + " (" + lang.getDisplayLabel() + ")")
                .orElse(getName());
    }
}

