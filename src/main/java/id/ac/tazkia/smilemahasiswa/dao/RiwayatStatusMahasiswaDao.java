package id.ac.tazkia.smilemahasiswa.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.RiwayatStatusMahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;

/**
 * DAO untuk RiwayatStatusMahasiswa
 *
 * Menyimpan query untuk mengelola riwayat status berjalan mahasiswa per semester
 */
public interface RiwayatStatusMahasiswaDao extends PagingAndSortingRepository<RiwayatStatusMahasiswa, String> {

    /**
     * Cari riwayat status berdasarkan mahasiswa dan tahun akademik
     */
    Optional<RiwayatStatusMahasiswa> findByMahasiswaAndTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    /**
     * Cari semua riwayat status mahasiswa, diurutkan berdasarkan tahun akademik
     */
    List<RiwayatStatusMahasiswa> findByMahasiswaOrderByTahunAkademikKodeTahunAkademikAsc(Mahasiswa mahasiswa);

    /**
     * Cari riwayat status mahasiswa berdasarkan status tertentu
     */
    List<RiwayatStatusMahasiswa> findByMahasiswaAndStatusBerjalanOrderByTahunAkademikKodeTahunAkademikAsc(
            Mahasiswa mahasiswa, StatusRecord status);

    /**
     * Cari semua mahasiswa dengan status tertentu di tahun akademik tertentu
     */
    List<RiwayatStatusMahasiswa> findByTahunAkademikAndStatusBerjalan(TahunAkademik tahunAkademik,
            StatusRecord status);

    /**
     * Cari dengan paging untuk laporan
     */
    Page<RiwayatStatusMahasiswa> findByTahunAkademikAndStatusBerjalan(TahunAkademik tahunAkademik,
            StatusRecord status, Pageable pageable);

    /**
     * Cari dengan paging berdasarkan prodi, tahun akademik, dan status
     */
    Page<RiwayatStatusMahasiswa> findByMahasiswaIdProdiAndTahunAkademikAndStatusBerjalan(
            Prodi prodi, TahunAkademik tahunAkademik, StatusRecord status, Pageable pageable);

    /**
     * Hitung jumlah mahasiswa berdasarkan status di tahun akademik tertentu
     */
    Long countByTahunAkademikAndStatusBerjalan(TahunAkademik tahunAkademik, StatusRecord status);

    /**
     * Hitung jumlah mahasiswa berdasarkan prodi, tahun akademik, dan status
     */
    Long countByMahasiswaIdProdiAndTahunAkademikAndStatusBerjalan(
            Prodi prodi, TahunAkademik tahunAkademik, StatusRecord status);

    /**
     * Cek apakah mahasiswa sudah punya riwayat status untuk tahun akademik tertentu
     */
    boolean existsByMahasiswaAndTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    /**
     * Query untuk rekap status mahasiswa per tahun akademik
     */
    @Query(value = "SELECT r.status_berjalan, COUNT(r.id) as jumlah " +
            "FROM riwayat_status_mahasiswa r " +
            "WHERE r.id_tahun_akademik = ?1 " +
            "GROUP BY r.status_berjalan", nativeQuery = true)
    List<Object[]> rekapStatusByTahunAkademik(String idTahunAkademik);

    /**
     * Query untuk rekap status mahasiswa per prodi dan tahun akademik
     */
    @Query(value = "SELECT p.nama_prodi, r.status_berjalan, COUNT(r.id) as jumlah " +
            "FROM riwayat_status_mahasiswa r " +
            "INNER JOIN mahasiswa m ON r.id_mahasiswa = m.id " +
            "INNER JOIN prodi p ON m.id_prodi = p.id " +
            "WHERE r.id_tahun_akademik = ?1 " +
            "GROUP BY p.id, p.nama_prodi, r.status_berjalan " +
            "ORDER BY p.nama_prodi, r.status_berjalan", nativeQuery = true)
    List<Object[]> rekapStatusByProdiAndTahunAkademik(String idTahunAkademik);

    /**
     * Query untuk mendapatkan mahasiswa yang tidak punya riwayat status di tahun akademik tertentu
     * (untuk auto-create riwayat status)
     */
    @Query(value = "SELECT m.id, m.nim, m.nama, m.id_prodi " +
            "FROM mahasiswa m " +
            "WHERE m.status_aktif IN ('AKTIF', 'CUTI') " +
            "AND m.angkatan <= (SELECT SUBSTR(ta.kode_tahun_akademik, 1, 4) FROM tahun_akademik ta WHERE ta.id = ?1) " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM riwayat_status_mahasiswa r " +
            "    WHERE r.id_mahasiswa = m.id AND r.id_tahun_akademik = ?1 " +
            ")", nativeQuery = true)
    List<Object[]> findMahasiswaWithoutRiwayatStatus(String idTahunAkademik);

    /**
     * Query untuk mendapatkan riwayat lengkap status mahasiswa (untuk ditampilkan di detail)
     */
    @Query(value = "SELECT r.id, ta.kode_tahun_akademik, ta.nama_tahun_akademik, ta.jenis, " +
            "r.status_berjalan, r.tanggal_mulai, r.tanggal_selesai, r.keterangan " +
            "FROM riwayat_status_mahasiswa r " +
            "INNER JOIN tahun_akademik ta ON r.id_tahun_akademik = ta.id " +
            "WHERE r.id_mahasiswa = ?1 " +
            "ORDER BY ta.kode_tahun_akademik ASC", nativeQuery = true)
    List<Object[]> getRiwayatStatusLengkap(String idMahasiswa);

    /**
     * Query untuk mencari mahasiswa yang cuti lebih dari X semester
     * (untuk deteksi putus studi)
     * Mahasiswa yang sudah status_keluar tidak akan ditampilkan
     */
    @Query(value = "SELECT m.id, m.nim, m.nama, COUNT(r.id) as jumlah_semester " +
            "FROM riwayat_status_mahasiswa r " +
            "INNER JOIN mahasiswa m ON r.id_mahasiswa = m.id " +
            "INNER JOIN tahun_akademik ta ON r.id_tahun_akademik = ta.id " +
            "WHERE r.status_berjalan = 'CUTI' " +
            "AND m.status_keluar IS NULL " +
            "GROUP BY m.id, m.nim, m.nama " +
            "HAVING COUNT(r.id) > ?1", nativeQuery = true)
    List<Object[]> findMahasiswaCutiLebihDari(int maxSemester);

    /**
     * Query untuk mencari mahasiswa yang tidak ada riwayat statusnya di semester terakhir
     * (potensi putus studi)
     * Mahasiswa yang sudah status_keluar tidak akan ditampilkan
     */
    @Query(value = "SELECT m.id, m.nim, m.nama, p.nama_prodi, m.angkatan " +
            "FROM mahasiswa m " +
            "INNER JOIN prodi p ON m.id_prodi = p.id " +
            "WHERE m.status_aktif = 'AKTIF' " +
            "AND m.status_keluar IS NULL " +
            "AND m.angkatan <= (SELECT SUBSTR(MAX(ta.kode_tahun_akademik), 1, 4) FROM tahun_akademik ta WHERE ta.status = 'AKTIF') " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM riwayat_status_mahasiswa r " +
            "    INNER JOIN tahun_akademik ta ON r.id_tahun_akademik = ta.id " +
            "    WHERE r.id_mahasiswa = m.id " +
            "    AND ta.status = 'AKTIF' " +
            ")", nativeQuery = true)
    List<Object[]> findMahasiswaTanpaRiwayatStatusSemesterAktif();

    /**
     * Query untuk bulk update status
     * Mengembalikan SEMUA mahasiswa (termasuk yang sudah punya status keluar)
     * Data structure: [r_id, m_id, m.nim, m.nama, p.nama_prodi, m.angkatan, m.jenis_masuk, r.status_berjalan, ta.nama_tahun_akademik, r.keterangan, m.status_keluar]
     */
    @Query(value = "SELECT r.id as r_id, m.id as m_id, m.nim, m.nama, p.nama_prodi, " +
            "m.angkatan, m.jenis_masuk, r.status_berjalan, ta.nama_tahun_akademik, r.keterangan, m.status_keluar " +
            "FROM mahasiswa m " +
            "INNER JOIN prodi p ON m.id_prodi = p.id " +
            "LEFT JOIN riwayat_status_mahasiswa r ON m.id = r.id_mahasiswa " +
            "AND (?1 IS NULL OR r.id_tahun_akademik = ?1) " +
            "LEFT JOIN tahun_akademik ta ON r.id_tahun_akademik = ta.id " +
            "WHERE (?2 IS NULL OR m.id_prodi = ?2) " +
            "AND (?3 IS NULL OR m.angkatan = ?3) " +
            "AND (?4 IS NULL OR r.status_berjalan = ?4) " +
            "ORDER BY m.nim ASC", nativeQuery = true)
    Page<Object[]> findMahasiswaForBulkUpdate(String idTahunAkademik, String idProdi,
            String angkatan, String status, Pageable pageable);

    /**
     * Query untuk mencari mahasiswa yang status berjalan LULUS tapi belum ada status keluar
     * dengan masa studi lebih dari 4 tahun
     * Data structure: [id_mahasiswa, nim, nama, nama_prodi, angkatan, id_riwayat, tahun_akademik, masa_studi_tahun]
     */
    @Query(value = "SELECT m.id as id_mahasiswa, m.nim, m.nama, p.nama_prodi, m.angkatan, " +
            "r.id as id_riwayat, ta.nama_tahun_akademik, " +
            "ROUND(DATEDIFF(CURDATE(), m.tanggal_masuk) / 365.25, 1) as masa_studi_tahun " +
            "FROM riwayat_status_mahasiswa r " +
            "INNER JOIN mahasiswa m ON r.id_mahasiswa = m.id " +
            "INNER JOIN prodi p ON m.id_prodi = p.id " +
            "INNER JOIN tahun_akademik ta ON r.id_tahun_akademik = ta.id " +
            "WHERE r.status_berjalan = 'LULUS' " +
            "AND m.status_keluar IS NULL " +
            "AND m.tanggal_masuk IS NOT NULL " +
            "AND DATEDIFF(CURDATE(), m.tanggal_masuk) > (4 * 365) " +
            "ORDER BY masa_studi_tahun DESC, m.nim ASC", nativeQuery = true)
    List<Object[]> findMahasiswaLulusTapiBelumStatusKeluar();

    // ========================================
    // SMART SUGGESTIONS - Update Status Berjalan
    // ========================================

    /**
     * Query untuk Smart Suggestions - Update Status Berjalan
     * Mengambil mahasiswa dengan kriteria:
     * - Status keluar = NULL (belum lulus/keluar)
     * - Status record = AKTIF
     * - tanggal_masuk <= tanggal_mulai tahun akademik (sudah kuliah di semester tersebut)
     *
     * Data structure:
     * [0] m.id
     * [1] m.nim
     * [2] m.nama
     * [3] p.nama_prodi
     * [4] m.angkatan
     * [5] masa_studi_tahun (dihitung dari tanggal_masuk ke tanggal_selesai tahun akademik)
     * [6] ada_krs (1=ada, 0=tidak ada)
     * [7] jumlah_krs_detail
     * [8] jumlah_hadir (total kehadiran mahasiswa di semua mk)
     * [9] min_tidak_hadir (MIN tidak hadir, hanya dari mk yang ada sesi dosen, NULL jika tidak ada sesi)
     * [10] jumlah_cuti_sebelumnya (dari riwayat_status_mahasiswa)
     * [11] id_riwayat_status (jika sudah ada, atau null)
     * [12] status_berjalan_saat_ini (jika sudah ada, atau null)
     * [13] m.tanggal_lulus (untuk cek rekomendasi LULUS)
     * [14] j.kode_jenjang (untuk cek batas studi: D3=10, S1=14, S2=8, S3=12 semester)
     *
     * Aturan rekomendasi (prioritas):
     * 1. LULUS - jika tanggal_lulus ada dan masuk range tahun akademik
     * 2. PUTUS_STUDI - jika masa studi > batas studi (sesuai jenjang)
     * 3. NONAKTIF - jika sudah cuti 2x dan tidak aktif
     * 4. CUTI - jika semua mk tidak hadir > 3x
     * 5. AKTIF - kondisi normal
     *
     * Batas studi per jenjang (sesuai PDDIKTI):
     * - D3: 10 semester (5 tahun)
        * - S1: 14 semester (7 years)
        * - S2: 8 semester (4 years)
        * - S3: 12 semester (6 years)
        */
    @Query(value = "SELECT " +
            "m.id as m_id, " +
            "m.nim, " +
            "m.nama, " +
            "p.nama_prodi, " +
            "m.angkatan, " +
            "ROUND(DATEDIFF(ta.tanggal_selesai, m.tanggal_masuk) / 365.25, 1) as masa_studi_tahun, " +
            "CASE WHEN k.id IS NOT NULL THEN 1 ELSE 0 END as ada_krs, " +
            "COALESCE(kd.jumlah_detail, 0) as jumlah_krs_detail, " +
            "COALESCE(cuti.jumlah_cuti, 0) as jumlah_cuti_sebelumnya, " +
            "rsm.id as id_riwayat_status, " +
            "rsm.status_berjalan as status_berjalan_saat_ini, " +
            "m.tanggal_lulus, " +
            "j.kode_jenjang " +
            "FROM mahasiswa m " +
            "INNER JOIN prodi p ON m.id_prodi = p.id " +
            "INNER JOIN jenjang j ON p.id_jenjang = j.id " +
            "INNER JOIN tahun_akademik ta ON ta.id = ?2 " +
            "LEFT JOIN krs k ON k.id_mahasiswa = m.id AND k.id_tahun_akademik = ?2 AND k.status = 'AKTIF' " +
            "LEFT JOIN ( " +
            "    SELECT krs_inner.id_mahasiswa, COUNT(kd.id) as jumlah_detail " +
            "    FROM krs krs_inner " +
            "    INNER JOIN krs_detail kd ON krs_inner.id = kd.id_krs AND kd.status = 'AKTIF' " +
            "    WHERE krs_inner.id_tahun_akademik = ?2 AND krs_inner.status = 'AKTIF' " +
            "    GROUP BY krs_inner.id_mahasiswa " +
            ") kd ON kd.id_mahasiswa = m.id " +
            "LEFT JOIN ( " +
            "    SELECT rsm_cuti.id_mahasiswa, COUNT(rsm_cuti.id) as jumlah_cuti " +
            "    FROM riwayat_status_mahasiswa rsm_cuti " +
            "    WHERE rsm_cuti.status_berjalan = 'CUTI' AND rsm_cuti.id_tahun_akademik != ?2 " +
            "    GROUP BY rsm_cuti.id_mahasiswa " +
            ") cuti ON cuti.id_mahasiswa = m.id " +
            "LEFT JOIN riwayat_status_mahasiswa rsm ON rsm.id_mahasiswa = m.id AND rsm.id_tahun_akademik = ?2 " +
            "WHERE m.status = 'AKTIF' " +
            "AND m.status_keluar IS NULL " +
            "AND m.tanggal_masuk IS NOT NULL " +
            "AND m.tanggal_masuk <= ta.tanggal_mulai " +
            "AND (DATEDIFF(ta.tanggal_selesai, m.tanggal_masuk) / 365.25) <= " +
            "    CASE j.kode_jenjang " +
            "        WHEN 'D3' THEN 5.5 " +
            "        WHEN 'S1' THEN 7.5 " +
            "        WHEN 'S2' THEN 4.5 " +
            "        WHEN 'S3' THEN 6.5 " +
            "        ELSE 7.5 " +
            "    END " +
            "AND (?1 IS NULL OR m.id_prodi = ?1) " +
            "ORDER BY m.nim ASC",
            countQuery = "SELECT COUNT(m.id) " +
            "FROM mahasiswa m " +
            "INNER JOIN prodi p ON m.id_prodi = p.id " +
            "INNER JOIN jenjang j ON p.id_jenjang = j.id " +
            "INNER JOIN tahun_akademik ta ON ta.id = ?2 " +
            "WHERE m.status = 'AKTIF' " +
            "AND m.status_keluar IS NULL " +
            "AND m.tanggal_masuk IS NOT NULL " +
            "AND m.tanggal_masuk <= ta.tanggal_mulai " +
            "AND (DATEDIFF(ta.tanggal_selesai, m.tanggal_masuk) / 365.25) <= " +
            "    CASE j.kode_jenjang " +
            "        WHEN 'D3' THEN 5.5 " +
            "        WHEN 'S1' THEN 7.5 " +
            "        WHEN 'S2' THEN 4.5 " +
            "        WHEN 'S3' THEN 6.5 " +
            "        ELSE 7.5 " +
            "    END " +
            "AND (?1 IS NULL OR m.id_prodi = ?1)",
            nativeQuery = true)
    Page<Object[]> findMahasiswaForSmartSuggestions(String idProdi, String idTahunAkademik, Pageable pageable);
}