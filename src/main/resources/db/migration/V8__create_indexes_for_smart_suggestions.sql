-- ========================================
-- Indexes untuk optimasi query Smart Suggestions
-- ========================================

-- Index untuk tabel mahasiswa
CREATE INDEX idx_mahasiswa_status ON mahasiswa(status);
CREATE INDEX idx_mahasiswa_status_keluar ON mahasiswa(status_keluar);
CREATE INDEX idx_mahasiswa_tanggal_masuk ON mahasiswa(tanggal_masuk);
CREATE INDEX idx_mahasiswa_id_prodi ON mahasiswa(id_prodi);
CREATE INDEX idx_mahasiswa_nim ON mahasiswa(nim);

-- Composite index untuk filter utama mahasiswa
CREATE INDEX idx_mahasiswa_filter ON mahasiswa(status, status_keluar, tanggal_masuk, id_prodi);

-- Index untuk tabel krs
CREATE INDEX idx_krs_id_mahasiswa ON krs(id_mahasiswa);
CREATE INDEX idx_krs_id_tahun_akademik ON krs(id_tahun_akademik);
CREATE INDEX idx_krs_status ON krs(status);

-- Composite index untuk join krs
CREATE INDEX idx_krs_mahasiswa_ta_status ON krs(id_mahasiswa, id_tahun_akademik, status);

-- Index untuk tabel krs_detail
CREATE INDEX idx_krs_detail_id_krs ON krs_detail(id_krs);
CREATE INDEX idx_krs_detail_status ON krs_detail(status);
CREATE INDEX idx_krs_detail_id_jadwal ON krs_detail(id_jadwal);
CREATE INDEX idx_krs_detail_id_mahasiswa ON krs_detail(id_mahasiswa);

-- Composite index untuk krs_detail
CREATE INDEX idx_krs_detail_krs_status ON krs_detail(id_krs, status);
CREATE INDEX idx_krs_detail_mahasiswa_status ON krs_detail(id_mahasiswa, status);

-- Index untuk tabel jadwal
CREATE INDEX idx_jadwal_id ON jadwal(id);

-- Index untuk tabel sesi_kuliah
CREATE INDEX idx_sesi_kuliah_id_jadwal ON sesi_kuliah(id_jadwal);
CREATE INDEX idx_sesi_kuliah_id_presensi_dosen ON sesi_kuliah(id_presensi_dosen);

-- Index untuk tabel presensi_dosen
CREATE INDEX idx_presensi_dosen_status_presensi ON presensi_dosen(status_presensi);
CREATE INDEX idx_presensi_dosen_status ON presensi_dosen(status);

-- Composite index untuk presensi_dosen
CREATE INDEX idx_presensi_dosen_filter ON presensi_dosen(status_presensi, status);

-- Index untuk tabel presensi_mahasiswa
CREATE INDEX idx_presensi_mahasiswa_id_sesi_kuliah ON presensi_mahasiswa(id_sesi_kuliah);
CREATE INDEX idx_presensi_mahasiswa_id_mahasiswa ON presensi_mahasiswa(id_mahasiswa);
CREATE INDEX idx_presensi_mahasiswa_status ON presensi_mahasiswa(status);
CREATE INDEX idx_presensi_mahasiswa_status_presensi ON presensi_mahasiswa(status_presensi);

-- Composite index untuk presensi_mahasiswa (critical untuk query presensi)
CREATE INDEX idx_presensi_mahasiswa_sesi_mhs_status ON presensi_mahasiswa(id_sesi_kuliah, id_mahasiswa, status);

-- Index untuk tabel riwayat_status_mahasiswa
CREATE INDEX idx_riwayat_status_id_mahasiswa ON riwayat_status_mahasiswa(id_mahasiswa);
CREATE INDEX idx_riwayat_status_id_tahun_akademik ON riwayat_status_mahasiswa(id_tahun_akademik);
CREATE INDEX idx_riwayat_status_status_berjalan ON riwayat_status_mahasiswa(status_berjalan);

-- Composite index untuk riwayat_status_mahasiswa
CREATE INDEX idx_riwayat_status_mhs_ta ON riwayat_status_mahasiswa(id_mahasiswa, id_tahun_akademik);
CREATE INDEX idx_riwayat_status_ta_status ON riwayat_status_mahasiswa(id_tahun_akademik, status_berjalan);

-- Index untuk tabel prodi
CREATE INDEX idx_prodi_id_jenjang ON prodi(id_jenjang);

-- Index untuk tabel tahun_akademik
CREATE INDEX idx_tahun_akademik_status ON tahun_akademik(status);
