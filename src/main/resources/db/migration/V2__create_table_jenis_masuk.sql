-- ========================================
-- Tabel Referensi: Jenis Masuk Mahasiswa
-- ========================================
-- Migration: V2
-- Description: Membuat tabel referensi untuk jenis masuk mahasiswa
-- ========================================

CREATE TABLE ref_jenis_masuk (
    kode VARCHAR(50) PRIMARY KEY COMMENT 'Kode unik untuk jenis masuk',
    nama VARCHAR(100) NOT NULL COMMENT 'Nama lengkap jenis masuk',
    keterangan VARCHAR(255) COMMENT 'Keterangan tambahan',
    urutan INT DEFAULT 0 COMMENT 'Urutan tampilan',
    status VARCHAR(20) DEFAULT 'AKTIF' COMMENT 'Status referensi',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabel referensi jenis masuk mahasiswa';

-- Insert data awal
INSERT INTO ref_jenis_masuk (kode, nama, keterangan, urutan) VALUES
('PESERTA_DIDIK_BARU', 'Peserta Didik Baru', 'Mahasiswa baru melalui jalur reguler/SNMPTN/SBMPTN', 1),
('PINDAHAN', 'Pindahan', 'Mahasiswa pindahan dari perguruan tinggi lain', 2),
('ALIH_JENJANG', 'Alih Jenjang', 'Mahasiswa alih jenjang (D3 ke S1, atau S1 ke S2)', 3),
('RPL_PEROLEHAN_SKS', 'RPL Perolehan SKS', 'RPL berdasarkan pengalaman kerja/pembelajaran', 4),
('RPL_TRANSFER_SKS', 'RPL Transfer SKS', 'RPL berdasarkan transfer mata kuliah sebelumnya', 5),
('DOUBLE_DEGREE', 'Double Degree', 'Program gelar ganda', 6),
('JOIN_DEGREE', 'Join Degree', 'Program gelar bersama', 7),
('FASTTRACK', 'Fasttrack', 'Program percepatan S1-S2', 8),
('PESERTA_NON_GELAR', 'Peserta Non Gelar', 'Peserta course non-gelar', 9);
