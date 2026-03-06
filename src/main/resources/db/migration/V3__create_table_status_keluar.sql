-- ========================================
-- Tabel Referensi: Status Keluar Mahasiswa
-- ========================================
-- Migration: V3
-- Description: Membuat tabel referensi untuk status keluar mahasiswa
-- ========================================

CREATE TABLE ref_status_keluar (
    kode VARCHAR(50) PRIMARY KEY COMMENT 'Kode unik untuk status keluar',
    nama VARCHAR(100) NOT NULL COMMENT 'Nama lengkap status keluar',
    keterangan VARCHAR(255) COMMENT 'Keterangan tambahan',
    urutan INT DEFAULT 0 COMMENT 'Urutan tampilan',
    status VARCHAR(20) DEFAULT 'AKTIF' COMMENT 'Status referensi',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabel referensi status keluar mahasiswa';

-- Insert data awal
INSERT INTO ref_status_keluar (kode, nama, keterangan, urutan) VALUES
('LULUS', 'Lulus', 'Mahasiswa lulus kuliah', 1),
('MENGUNDURKAN_DIRI', 'Mengundurkan Diri', 'Mahasiswa mengundurkan diri atas kemauan sendiri', 2),
('MENINGGAL_DUNIA', 'Meninggal Dunia', 'Mahasiswa meninggal dunia', 3),
('PUTUS_STUDI', 'Putus Studi', 'Mahasiswa putus studi (tidak aktif melebihi batas waktu)', 4),
('DIKELUARKAN', 'Dikeluarkan', 'Mahasiswa dikeluarkan karena pelanggaran', 5),
('MUTASI', 'Mutasi', 'Mahasiswa pindah ke perguruan tinggi lain', 6);
