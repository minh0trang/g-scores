-- ==========================================================
-- V4: Create Precomputed Student Block Scores Table & Fast Indexes
-- ==========================================================

-- 1. Create table without secondary indexes to maximize bulk insert speed
CREATE TABLE IF NOT EXISTS student_block_scores (
    id            BIGSERIAL PRIMARY KEY,
    student_id    BIGINT        NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    province_code VARCHAR(2)    NOT NULL REFERENCES provinces (code),
    block_code    VARCHAR(10)   NOT NULL,
    total_score   NUMERIC(5, 2) NOT NULL
);

-- 2. Backfill precomputed admission block scores in a single aggregation pass
WITH scores_pivot AS (
    SELECT
        es.student_id,
        MAX(CASE WHEN sub.code = 'TOAN' THEN es.score END) AS toan,
        MAX(CASE WHEN sub.code = 'NGU_VAN' THEN es.score END) AS van,
        MAX(CASE WHEN sub.code = 'NGOAI_NGU' THEN es.score END) AS nn,
        MAX(CASE WHEN sub.code = 'VAT_LI' THEN es.score END) AS ly,
        MAX(CASE WHEN sub.code = 'HOA_HOC' THEN es.score END) AS hoa,
        MAX(CASE WHEN sub.code = 'SINH_HOC' THEN es.score END) AS sinh,
        MAX(CASE WHEN sub.code = 'LICH_SU' THEN es.score END) AS su,
        MAX(CASE WHEN sub.code = 'DIA_LI' THEN es.score END) AS dia
    FROM exam_scores es
    JOIN subjects sub ON sub.id = es.subject_id
    GROUP BY es.student_id
),
student_scores AS (
    SELECT
        sp.student_id,
        s.province_code,
        s.language_code,
        sp.toan,
        sp.van,
        sp.nn,
        sp.ly,
        sp.hoa,
        sp.sinh,
        sp.su,
        sp.dia
    FROM scores_pivot sp
    JOIN students s ON s.id = sp.student_id
)
INSERT INTO student_block_scores (student_id, province_code, block_code, total_score)
SELECT
    s.student_id,
    s.province_code,
    b.block_code,
    b.total_score
FROM student_scores s
CROSS JOIN LATERAL (
    VALUES
        ('A00', ROUND(s.toan + s.ly + s.hoa, 2), s.toan IS NOT NULL AND s.ly IS NOT NULL AND s.hoa IS NOT NULL),
        ('B00', ROUND(s.toan + s.hoa + s.sinh, 2), s.toan IS NOT NULL AND s.hoa IS NOT NULL AND s.sinh IS NOT NULL),
        ('C00', ROUND(s.van + s.su + s.dia, 2), s.van IS NOT NULL AND s.su IS NOT NULL AND s.dia IS NOT NULL),
        ('A01', ROUND(s.toan + s.ly + s.nn, 2), s.toan IS NOT NULL AND s.ly IS NOT NULL AND s.nn IS NOT NULL AND s.language_code = 'N1'),
        ('D0' || SUBSTRING(COALESCE(s.language_code, 'N1'), 2, 1), ROUND(s.toan + s.van + s.nn, 2), s.toan IS NOT NULL AND s.van IS NOT NULL AND s.nn IS NOT NULL AND s.language_code IN ('N1', 'N2', 'N3', 'N4', 'N5', 'N6', 'N7'))
) AS b(block_code, total_score, is_valid)
WHERE b.is_valid;

-- 3. Add unique constraint and high-performance composite indexes post-insert
ALTER TABLE student_block_scores
    ADD CONSTRAINT uq_student_block UNIQUE (student_id, block_code);

CREATE INDEX IF NOT EXISTS idx_block_score_nat
    ON student_block_scores (block_code, total_score DESC);

CREATE INDEX IF NOT EXISTS idx_block_score_prov
    ON student_block_scores (block_code, province_code, total_score DESC);

CREATE INDEX IF NOT EXISTS idx_block_score_student_id
    ON student_block_scores (student_id);

