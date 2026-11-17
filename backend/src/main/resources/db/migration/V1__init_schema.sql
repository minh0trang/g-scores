-- ==========================================================
-- V1: Initialize G-Scores Normalized 3-Table Relational Schema
-- ==========================================================

CREATE TABLE IF NOT EXISTS students (
    id            BIGSERIAL PRIMARY KEY,
    sbd           VARCHAR(16) NOT NULL,
    language_code VARCHAR(10),
    CONSTRAINT uq_students_sbd UNIQUE (sbd)
);

CREATE TABLE IF NOT EXISTS subjects (
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    CONSTRAINT uq_subjects_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS exam_scores (
    id         BIGSERIAL PRIMARY KEY,
    student_id BIGINT        NOT NULL,
    subject_id BIGINT        NOT NULL,
    score      NUMERIC(4, 2) NOT NULL,
    CONSTRAINT fk_exam_scores_student
        FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_exam_scores_subject
        FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE RESTRICT,
    CONSTRAINT uq_exam_scores_student_subject
        UNIQUE (student_id, subject_id),
    CONSTRAINT chk_exam_scores_range
        CHECK (score >= 0.00 AND score <= 10.00)
);

-- Indexes for sub-second 4-tier score distribution and Group A00 Top 10 queries
CREATE INDEX IF NOT EXISTS idx_exam_scores_subject_score
    ON exam_scores (subject_id, score);

CREATE INDEX IF NOT EXISTS idx_exam_scores_student_id
    ON exam_scores (student_id);

