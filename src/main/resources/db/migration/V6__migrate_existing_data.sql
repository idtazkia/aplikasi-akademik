-- ========================================
-- Migrate Data Existing
-- ========================================
-- Migration: V6
-- Description: Migrasi data existing ke field baru
-- ========================================

-- ========================================
-- 1. Set default jenis_masuk untuk data yang sudah ada
-- ========================================
-- Untuk mahasiswa yang sudah ada, set jenis_masuk = PESERTA_DIDIK_BARU
UPDATE mahasiswa
SET jenis_masuk = 'PESERTA_DIDIK_BARU'
WHERE jenis_masuk IS NULL;

-- ========================================
-- 2. Migrasi status keluar dari status_aktif
-- ========================================
-- Untuk mahasiswa dengan status_aktif = LULUS, set status_keluar = LULUS
UPDATE mahasiswa
SET status_keluar = 'LULUS',
    tanggal_keluar = tanggal_lulus,
    id_tahun_akademik_keluar = id_tahun_mengundurkan_diri,
    status_keluar_update_date = NOW()
WHERE status_aktif = 'LULUS' AND status_keluar IS NULL;

-- Untuk mahasiswa dengan status_aktif = MENGUNDURKAN_DIRI atau KELUAR
UPDATE mahasiswa
SET status_keluar = CASE
    WHEN status_aktif = 'MENGUNDURKAN_DIRI' THEN 'MENGUNDURKAN_DIRI'
    WHEN status_aktif = 'KELUAR' THEN 'MENGUNDURKAN_DIRI'
    ELSE status_keluar
END,
    tanggal_keluar = COALESCE(tanggal_mengundurkan_diri, tanggal_keluar),
    id_tahun_akademik_keluar = COALESCE(id_tahun_mengundurkan_diri, id_tahun_akademik_keluar),
    status_keluar_update_date = NOW()
WHERE status_aktif IN ('MENGUNDURKAN_DIRI', 'KELUAR') AND status_keluar IS NULL;

-- ========================================
-- 3. Migrasi riwayat status dari KRS
-- ========================================
-- Buat record riwayat_status_mahasiswa berdasarkan data KRS yang sudah ada
-- Hanya untuk KRS yang id_tahun_akademik dan id_mahasiswa valid
INSERT INTO riwayat_status_mahasiswa (
    id,
    id_mahasiswa,
    id_tahun_akademik,
    status_berjalan,
    tanggal_mulai,
    keterangan,
    created_at,
    user_insert,
    date_insert
)
SELECT
    UUID() as id,
    k.id_mahasiswa,
    k.id_tahun_akademik,
    k.status as status_berjalan,
    CURRENT_DATE() as tanggal_mulai,
    CONCAT('Diimpor dari KRS, Semester: ', k.semester) as keterangan,
    NOW() as created_at,
    'system' as user_insert,
    NOW() as date_insert
FROM krs k
WHERE k.id_mahasiswa IS NOT NULL
  AND k.id_tahun_akademik IS NOT NULL
  AND k.status IS NOT NULL
  AND EXISTS (SELECT 1 FROM tahun_akademik ta WHERE ta.id = k.id_tahun_akademik)
  AND EXISTS (SELECT 1 FROM mahasiswa m WHERE m.id = k.id_mahasiswa)
ON DUPLICATE KEY UPDATE
    status_berjalan = VALUES(status_berjalan),
    tanggal_mulai = VALUES(tanggal_mulai),
    keterangan = VALUES(keterangan),
    updated_at = NOW();

-- ========================================
-- 4. Handle mahasiswa yang belum punya riwayat status
-- ========================================
-- Untuk mahasiswa aktif yang belum punya riwayat status untuk periode aktif,
-- buat record berdasarkan status_aktif
-- Note: Ini perlu disesuaikan dengan logika bisnis yang ada

-- ========================================
-- 5. Catatan: Field lama yang akan di-deprecate
-- ========================================
-- Field-field berikut akan di-deprecate setelah migrasi berhasil:
-- - status_aktif (diganti dengan kombinasi: status_keluar + riwayat_status_mahasiswa)
-- - tanggal_mengundurkan_diri (diganti dengan tanggal_keluar)
-- - id_tahun_mengundurkan_diri (diganti dengan id_tahun_akademik_keluar)
-- - status (enum StatusRecord di entity Mahasiswa) - akan di-review penggunaannya
