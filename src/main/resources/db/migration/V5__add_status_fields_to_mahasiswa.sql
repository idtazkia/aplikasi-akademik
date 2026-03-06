-- ========================================
-- Alter Table: Tambah Field Status ke Tabel Mahasiswa
-- ========================================
-- Migration: V5
-- Description: Menambahkan field untuk status keluar dan informasi terkait
-- Note: jenis_masuk sudah ada, jadi tidak perlu ditambahkan lagi
-- ========================================

-- Tambah field status keluar (menggantikan logika status_aktif untuk status keluar)
ALTER TABLE mahasiswa
ADD COLUMN status_keluar VARCHAR(50) DEFAULT NULL COMMENT 'Status keluar mahasiswa (LULUS, MENGUNDURKAN_DIRI, dll)' AFTER status_aktif;

-- Tambah field tanggal keluar (lebih umum dari tanggal_lulus/tanggal_mengundurkan_diri)
ALTER TABLE mahasiswa
ADD COLUMN tanggal_keluar DATE DEFAULT NULL COMMENT 'Tanggal mahasiswa keluar/lulus' AFTER tanggal_lulus;

-- Tambah field tahun akademik keluar (lebih umum dari id_tahun_mengundurkan_diri)
ALTER TABLE mahasiswa
ADD COLUMN id_tahun_akademik_keluar VARCHAR(36) DEFAULT NULL COMMENT 'Tahun akademik saat mahasiswa keluar' AFTER id_tahun_mengundurkan_diri;

-- Tambah field alasan keluar
ALTER TABLE mahasiswa
ADD COLUMN alasan_keluar TEXT DEFAULT NULL COMMENT 'Alasan mahasiswa keluar (opsional)' AFTER tanggal_keluar;

-- Tambah field untuk tracking update status keluar
ALTER TABLE mahasiswa
ADD COLUMN status_keluar_update_user VARCHAR(100) DEFAULT NULL COMMENT 'User yang mengupdate status keluar' AFTER status_update_user;

-- Tambah field tanggal update status keluar
ALTER TABLE mahasiswa
ADD COLUMN status_keluar_update_date DATETIME DEFAULT NULL COMMENT 'Tanggal update status keluar' AFTER status_keluar_update_user;

-- Index untuk performa
CREATE INDEX idx_status_keluar ON mahasiswa(status_keluar);
CREATE INDEX idx_tanggal_keluar ON mahasiswa(tanggal_keluar);
CREATE INDEX idx_tahun_akademik_keluar ON mahasiswa(id_tahun_akademik_keluar);
