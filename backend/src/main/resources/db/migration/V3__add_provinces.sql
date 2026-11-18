-- ==========================================================
-- V3: Add Provinces Table, Seed 63 Provinces, and Link Students
-- ==========================================================

CREATE TABLE IF NOT EXISTS provinces (
    code VARCHAR(2) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

INSERT INTO provinces (code, name) VALUES
('01', 'Hà Nội'),
('02', 'Hà Giang'),
('03', 'Cao Bằng'),
('04', 'Bắc Kạn'),
('05', 'Tuyên Quang'),
('06', 'Lào Cai'),
('07', 'Điện Biên'),
('08', 'Lai Châu'),
('09', 'Sơn La'),
('10', 'Yên Bái'),
('11', 'Hòa Bình'),
('12', 'Thái Nguyên'),
('13', 'Lạng Sơn'),
('14', 'Quảng Ninh'),
('15', 'Bắc Giang'),
('16', 'Phú Thọ'),
('17', 'Vĩnh Phúc'),
('18', 'Bắc Ninh'),
('19', 'Hải Dương'),
('21', 'Hải Phòng'),
('22', 'Hưng Yên'),
('23', 'Thái Bình'),
('24', 'Hà Nam'),
('25', 'Nam Định'),
('26', 'Ninh Bình'),
('27', 'Thanh Hóa'),
('28', 'Nghệ An'),
('29', 'Hà Tĩnh'),
('30', 'Quảng Bình'),
('31', 'Quảng Trị'),
('32', 'Thừa Thiên Huế'),
('33', 'Đà Nẵng'),
('34', 'Quảng Nam'),
('35', 'Quảng Ngãi'),
('36', 'Bình Định'),
('37', 'Phú Yên'),
('38', 'Khánh Hòa'),
('39', 'Ninh Thuận'),
('40', 'Bình Thuận'),
('41', 'Kon Tum'),
('42', 'Gia Lai'),
('43', 'Đắk Lắk'),
('44', 'Đắk Nông'),
('45', 'Lâm Đồng'),
('46', 'Bình Phước'),
('47', 'Tây Ninh'),
('48', 'Bình Dương'),
('49', 'Đồng Nai'),
('50', 'Bà Rịa - Vũng Tàu'),
('51', 'TP. Hồ Chí Minh'),
('52', 'Long An'),
('53', 'Tiền Giang'),
('54', 'Bến Tre'),
('55', 'Trà Vinh'),
('56', 'Vĩnh Long'),
('57', 'Đồng Tháp'),
('58', 'An Giang'),
('59', 'Kiên Giang'),
('60', 'Cần Thơ'),
('61', 'Hậu Giang'),
('62', 'Sóc Trăng'),
('63', 'Bạc Liêu'),
('64', 'Cà Mau')
ON CONFLICT (code) DO NOTHING;

-- Add province_code column to students
ALTER TABLE students ADD COLUMN IF NOT EXISTS province_code VARCHAR(2);

-- Backfill province_code from the first 2 characters of sbd
UPDATE students
SET province_code = LEFT(sbd, 2)
WHERE province_code IS NULL;

-- Enforce NOT NULL and foreign key constraint
ALTER TABLE students
    ALTER COLUMN province_code SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_students_province'
    ) THEN
        ALTER TABLE students
            ADD CONSTRAINT fk_students_province
            FOREIGN KEY (province_code) REFERENCES provinces(code);
    END IF;
END $$;

-- Index for filtering students by province
CREATE INDEX IF NOT EXISTS idx_students_province_code ON students (province_code);

