-- ==========================================================
-- V2: Seed 9 Official THPT 2024 Exam Subjects
-- (Single unified NGOAI_NGU entry; language_code N1..N7 is stored on students)
-- ==========================================================

INSERT INTO subjects (code, name)
VALUES ('TOAN', 'Toán'),
       ('NGU_VAN', 'Ngữ văn'),
       ('NGOAI_NGU', 'Ngoại ngữ'),
       ('VAT_LI', 'Vật lí'),
       ('HOA_HOC', 'Hóa học'),
       ('SINH_HOC', 'Sinh học'),
       ('LICH_SU', 'Lịch sử'),
       ('DIA_LI', 'Địa lí'),
       ('GDCD', 'GDCD')
ON CONFLICT (code) DO NOTHING;

