-- ========================================
-- Tabel: Riwayat Status Mahasiswa per Semester
-- ========================================
-- Migration: V4
-- Description: Membuat tabel untuk menyimpan riwayat status berjalan mahasiswa per semester
-- ========================================

CREATE TABLE riwayat_status_mahasiswa (
    id VARCHAR(45) NOT NULL PRIMARY KEY COMMENT 'ID unik riwayat status (UUID)',
    id_mahasiswa VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ID mahasiswa',
    id_tahun_akademik VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ID tahun akademik',
    status_berjalan VARCHAR(50) NOT NULL COMMENT 'Status berjalan mahasiswa (AKTIF, CUTI, NONAKTIF, dll)',
    tanggal_mulai DATE DEFAULT NULL COMMENT 'Tanggal status mulai',
    tanggal_selesai DATE DEFAULT NULL COMMENT 'Tanggal status selesai (opsional)',
    keterangan TEXT COMMENT 'Keterangan tambahan terkait status',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Waktu record dibuat',
    created_by VARCHAR(100) DEFAULT NULL COMMENT 'User yang membuat record',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Waktu record diupdate',
    updated_by VARCHAR(100) DEFAULT NULL COMMENT 'User yang mengupdate record',
    user_insert VARCHAR(45) DEFAULT NULL COMMENT 'User yang insert',
    user_update VARCHAR(45) DEFAULT NULL COMMENT 'User yang update',
    date_insert DATETIME DEFAULT NULL COMMENT 'Tanggal insert',
    date_update DATETIME DEFAULT NULL COMMENT 'Tanggal update',

    -- Foreign Keys
    FOREIGN KEY (id_mahasiswa) REFERENCES mahasiswa(id) ON DELETE CASCADE,
    FOREIGN KEY (id_tahun_akademik) REFERENCES tahun_akademik(id) ON DELETE CASCADE,

    -- Unique Constraint: Satu mahasiswa hanya punya satu status per tahun akademik
    UNIQUE KEY uk_mahasiswa_periode (id_mahasiswa, id_tahun_akademik),

    -- Indexes untuk performa query
    INDEX idx_mahasiswa (id_mahasiswa),
    INDEX idx_tahun_akademik (id_tahun_akademik),
    INDEX idx_status_berjalan (status_berjalan),
    INDEX idx_tanggal_mulai (tanggal_mulai)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
COMMENT='Tabel riwayat status berjalan mahasiswa per semester';
