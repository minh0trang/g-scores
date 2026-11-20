-- ==========================================================
-- V7: Drop Redundant Single-Column Indexes Covered by Composite UNIQUE Constraints
-- - idx_exam_scores_student_id is covered by uq_exam_scores_student_subject (student_id, subject_id)
-- - idx_block_score_student_id is covered by uq_student_block (student_id, block_code)
-- ==========================================================

DROP INDEX IF EXISTS idx_exam_scores_student_id;
DROP INDEX IF EXISTS idx_block_score_student_id;

