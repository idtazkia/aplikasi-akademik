-- ========================================
-- Create Additional Indexes for Performance
-- ========================================
-- Migration: V7
-- Description: Membuat index tambahan untuk performa query laporan status
-- ========================================

-- ========================================
-- Index untuk tabel riwayat_status_mahasiswa
-- ========================================

-- Composite index untuk query berdasarkan mahasiswa dan tahun akademik (sudah ada sebagai unique key)
-- UNIQUE KEY uk_mahasiswa_periode (id_mahasiswa, id_tahun_akademik)

-- Composite index untuk query filter status berjalan
CREATE INDEX idx_mahasiswa_status ON riwayat_status_mahasiswa(id_mahasiswa, status_berjalan);

-- Composite index untuk query laporan per tahun akademik dan status
CREATE INDEX idx_tahun_akademik_status ON riwayat_status_mahasiswa(id_tahun_akademik, status_berjalan);

-- Composite index untuk query riwayat berdasarkan mahasiswa (ordered by tahun akademik)
CREATE INDEX idx_mahasiswa_tahun_akademik ON riwayat_status_mahasiswa(id_mahasiswa, id_tahun_akademik);

-- Index untuk query berdasarkan tanggal
CREATE INDEX idx_tanggal_mulai_status ON riwayat_status_mahasiswa(tanggal_mulai, status_berjalan);

-- ========================================
-- Index untuk tabel mahasiswa (query status keluar)
-- ========================================

-- Composite index untuk query mahasiswa keluar per tahun akademik
CREATE INDEX idx_status_keluar_tahun_akademik ON mahasiswa(status_keluar, id_tahun_akademik_keluar);

-- Composite index untuk query mahasiswa berdasarkan jenis masuk
CREATE INDEX idx_jenis_masuk_angkatan ON mahasiswa(jenis_masuk, angkatan);

-- Composite index untuk query mahasiswa lulus
CREATE INDEX idx_status_keluar_tanggal ON mahasiswa(status_keluar, tanggal_keluar);

-- ========================================
-- Index untuk query laporan gabungan
-- ========================================

-- Index untuk join mahasiswa - riwayat_status_mahasiswa
-- Sudah ada: idx_mahasiswa di riwayat_status_mahasiswa

-- Index untuk filter prodi dan status
CREATE INDEX idx_prodi_status_keluar ON mahasiswa(id_prodi, status_keluar);
