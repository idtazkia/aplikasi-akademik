-- ========================================
-- Additional indexes untuk optimasi Smart Suggestions query
-- ========================================

-- Composite index untuk krs dengan filter tahun akademik dan status
CREATE INDEX idx_krs_tahun_status_mahasiswa ON krs(id_tahun_akademik, status, id_mahasiswa);

-- Composite index untuk krs_detail dengan join ke krs
CREATE INDEX idx_krs_detail_krs_status_jadwal ON krs_detail(id_krs, status, id_jadwal);
CREATE INDEX idx_krs_detail_mahasiswa_jadwal ON krs_detail(id_mahasiswa, id_jadwal, status);

-- Composite index untuk sesi_kuliah dengan join ke presensi_dosen
CREATE INDEX idx_sesi_kuliah_jadwal_presensi ON sesi_kuliah(id_jadwal, id_presensi_dosen);

-- Composite index untuk presensi_mahasiswa
CREATE INDEX idx_presensi_mahasiswa_sesi_status ON presensi_mahasiswa(id_sesi_kuliah, status, id_mahasiswa);
CREATE INDEX idx_presensi_mahasiswa_mhs_sesi_status ON presensi_mahasiswa(id_mahasiswa, id_sesi_kuliah, status);

-- Composite index untuk riwayat_status_mahasiswa
CREATE INDEX idx_riwayat_status_mhs_status ON riwayat_status_mahasiswa(id_mahasiswa, status_berjalan);
CREATE INDEX idx_riwayat_status_status_tahun ON riwayat_status_mahasiswa(status_berjalan, id_tahun_akademik);
