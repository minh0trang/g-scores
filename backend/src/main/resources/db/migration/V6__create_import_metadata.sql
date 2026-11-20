-- ==========================================================
-- V6: Create Import Completeness Metadata Table
-- (Never marks pre-existing legacy rows as COMPLETED automatically)
-- ==========================================================

CREATE TABLE IF NOT EXISTS import_metadata (
    dataset_key        VARCHAR(64) PRIMARY KEY,
    status             VARCHAR(32) NOT NULL
        CONSTRAINT chk_import_metadata_status
            CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED', 'UNVERIFIED_LEGACY')),
    source_file        VARCHAR(255),
    source_checksum    VARCHAR(64),
    source_row_count   BIGINT,
    imported_students  BIGINT      NOT NULL DEFAULT 0,
    imported_scores    BIGINT      NOT NULL DEFAULT 0,
    precomputed_blocks BIGINT      NOT NULL DEFAULT 0,
    failed_rows        BIGINT      NOT NULL DEFAULT 0,
    verified_by        VARCHAR(64),
    started_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at       TIMESTAMPTZ,
    error_message      TEXT
);

-- If rows already exist before import_metadata existed, record them strictly as UNVERIFIED_LEGACY.
INSERT INTO import_metadata (
    dataset_key,
    status,
    source_file,
    imported_students,
    imported_scores,
    precomputed_blocks,
    started_at,
    completed_at,
    error_message
)
SELECT
    'thpt_2024',
    'UNVERIFIED_LEGACY',
    'legacy-pre-v6-unverified',
    (SELECT COUNT(*) FROM students),
    (SELECT COUNT(*) FROM exam_scores),
    (SELECT COUNT(*) FROM student_block_scores),
    NOW(),
    NULL,
    'Pre-existing data detected prior to import_metadata tracking; requires clean re-import from CSV to verify completeness.'
WHERE EXISTS (SELECT 1 FROM students)
   OR EXISTS (SELECT 1 FROM exam_scores)
   OR EXISTS (SELECT 1 FROM student_block_scores)
ON CONFLICT (dataset_key) DO NOTHING;

