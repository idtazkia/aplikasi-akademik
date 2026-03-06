package id.ac.tazkia.smilemahasiswa.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.RiwayatStatusMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dto.BulkUpdateStatusDto;
import id.ac.tazkia.smilemahasiswa.dto.SmartSuggestionDto;
import id.ac.tazkia.smilemahasiswa.dto.StatusBerjalanDto;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.RiwayatStatusMahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusKeluar;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Service untuk mengelola Riwayat Status Mahasiswa
 *
 * Menangani status berjalan mahasiswa per semester/ tahun akademik
 */
@Service
@Transactional
@Slf4j
public class RiwayatStatusService {

    @Autowired
    private RiwayatStatusMahasiswaDao riwayatStatusMahasiswaDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    /**
     * Buat atau update riwayat status mahasiswa
     */
    public RiwayatStatusMahasiswa simpanRiwayatStatus(StatusBerjalanDto dto, String username) {
        Mahasiswa mahasiswa = mahasiswaDao.findById(dto.getIdMahasiswa())
                .orElseThrow(() -> new RuntimeException("Mahasiswa tidak ditemukan"));

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(dto.getIdTahunAkademik())
                .orElseThrow(() -> new RuntimeException("Tahun akademik tidak ditemukan"));

        // Cek apakah sudah ada riwayat status untuk periode ini
        Optional<RiwayatStatusMahasiswa> existing = riwayatStatusMahasiswaDao
                .findByMahasiswaAndTahunAkademik(mahasiswa, tahunAkademik);

        RiwayatStatusMahasiswa riwayatStatus;
        if (existing.isPresent()) {
            // Update existing
            riwayatStatus = existing.get();
            log.info("Update riwayat status mahasiswa {} untuk tahun akademik {}",
                    mahasiswa.getNim(), tahunAkademik.getNamaTahunAkademik());
        } else {
            // Create new
            riwayatStatus = new RiwayatStatusMahasiswa();
            riwayatStatus.setMahasiswa(mahasiswa);
            riwayatStatus.setTahunAkademik(tahunAkademik);
            riwayatStatus.setCreatedAt(LocalDateTime.now());
            log.info("Buat riwayat status baru untuk mahasiswa {} tahun akademik {}",
                    mahasiswa.getNim(), tahunAkademik.getNamaTahunAkademik());
        }

        // Set values
        riwayatStatus.setStatusBerjalan(dto.getStatusBerjalan());
        riwayatStatus.setTanggalMulai(dto.getTanggalMulai());
        riwayatStatus.setTanggalSelesai(dto.getTanggalSelesai());
        riwayatStatus.setKeterangan(dto.getKeterangan());
        riwayatStatus.setUpdatedBy(username);
        riwayatStatus.setUpdatedAt(LocalDateTime.now());

        // Simpan riwayat status
        RiwayatStatusMahasiswa saved = riwayatStatusMahasiswaDao.save(riwayatStatus);

        // Jika status baru adalah status keluar, update juga mahasiswa
        if (isStatusKeluar(dto.getStatusBerjalan())) {
            StatusKeluar statusKeluar = convertToStatusKeluar(dto.getStatusBerjalan());
            if (statusKeluar != null) {
                mahasiswa.setStatusKeluar(statusKeluar);
                mahasiswa.setTanggalKeluar(dto.getTanggalKeluar() != null
                    ? dto.getTanggalKeluar()
                    : LocalDate.now());
                mahasiswa.setTahunAkademikKeluar(tahunAkademik);
                mahasiswa.setStatusKeluarUpdateUser(username);
                mahasiswa.setStatusKeluarUpdateDate(LocalDateTime.now());
                mahasiswaDao.save(mahasiswa);
                log.info("Updated status keluar {} for mahasiswa {}",
                    statusKeluar, mahasiswa.getNim());
            }
        }

        return saved;
    }

    /**
     * Ambil riwayat status lengkap mahasiswa
     */
    public List<RiwayatStatusMahasiswa> getRiwayatStatusLengkap(String idMahasiswa) {
        Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa)
                .orElseThrow(() -> new RuntimeException("Mahasiswa tidak ditemukan"));
        return riwayatStatusMahasiswaDao.findByMahasiswaOrderByTahunAkademikKodeTahunAkademikAsc(mahasiswa);
    }

    /**
     * Ambil riwayat status mahasiswa untuk tahun akademik tertentu
     */
    public Optional<RiwayatStatusMahasiswa> getRiwayatStatus(String idMahasiswa, String idTahunAkademik) {
        Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa)
                .orElseThrow(() -> new RuntimeException("Mahasiswa tidak ditemukan"));

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(idTahunAkademik)
                .orElseThrow(() -> new RuntimeException("Tahun akademik tidak ditemukan"));

        return riwayatStatusMahasiswaDao.findByMahasiswaAndTahunAkademik(mahasiswa, tahunAkademik);
    }

    /**
     * Bulk update status mahasiswa
     */
    /**
     * Result class untuk bulk update status
     */
    public static class BulkUpdateResult {
        private int updated;
        private int skipped;
        private List<String> skippedMessages;

        public BulkUpdateResult(int updated, int skipped, List<String> skippedMessages) {
            this.updated = updated;
            this.skipped = skipped;
            this.skippedMessages = skippedMessages;
        }

        public int getUpdated() { return updated; }
        public int getSkipped() { return skipped; }
        public List<String> getSkippedMessages() { return skippedMessages; }
    }

    /**
     * Cek apakah StatusRecord adalah status keluar
     */
    private boolean isStatusKeluar(StatusRecord status) {
        if (status == null) return false;
        Set<StatusRecord> statusKeluarSet = new HashSet<>(Arrays.asList(
            StatusRecord.LULUS,
            StatusRecord.MENGUNDURKAN_DIRI,
            StatusRecord.MENINGGAL_DUNIA,
            StatusRecord.PUTUS_STUDI,
            StatusRecord.DIKELUARKAN,
            StatusRecord.MUTASI
        ));
        return statusKeluarSet.contains(status);
    }

    /**
     * Convert StatusRecord ke StatusKeluar
     */
    private StatusKeluar convertToStatusKeluar(StatusRecord status) {
        if (status == null) return null;
        try {
            return StatusKeluar.valueOf(status.name());
        } catch (IllegalArgumentException e) {
            log.warn("StatusRecord {} tidak bisa di-convert ke StatusKeluar", status);
            return null;
        }
    }

    public BulkUpdateResult bulkUpdateStatus(BulkUpdateStatusDto dto, String username) {
        List<RiwayatStatusMahasiswa> results = new ArrayList<>();
        List<String> skippedMessages = new ArrayList<>();

        log.info("=== BULK UPDATE SERVICE ===");
        log.info("getIdMahasiswa: {}", dto.getIdMahasiswa());
        log.info("getIdTahunAkademik: {}", dto.getIdTahunAkademik());
        log.info("getStatusBaru: {}", dto.getStatusBaru());

        // Jika ada idMahasiswa yang dipilih (ini ID mahasiswa, bukan ID riwayat status)
        if (dto.getIdMahasiswa() != null && !dto.getIdMahasiswa().isEmpty()) {
            TahunAkademik tahunAkademik = null;
            if (dto.getIdTahunAkademik() != null) {
                tahunAkademik = tahunAkademikDao.findById(dto.getIdTahunAkademik()).orElse(null);
            }

            for (String idMahasiswa : dto.getIdMahasiswa()) {
                try {
                    log.info("Processing mahasiswa id: {}", idMahasiswa);
                    Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa).orElse(null);
                    if (mahasiswa == null) {
                        log.warn("Mahasiswa not found: {}", idMahasiswa);
                        skippedMessages.add("Mahasiswa tidak ditemukan: " + idMahasiswa);
                        continue;
                    }

                    // Cari riwayat status berdasarkan mahasiswa dan tahun akademik
                    RiwayatStatusMahasiswa riwayat = null;
                    if (tahunAkademik != null) {
                        Optional<RiwayatStatusMahasiswa> existing = riwayatStatusMahasiswaDao
                                .findByMahasiswaAndTahunAkademik(mahasiswa, tahunAkademik);
                        riwayat = existing.orElse(null);
                    }

                    if (riwayat == null && tahunAkademik != null) {
                        // Jika belum ada riwayat status, buat baru
                        log.info("Creating new riwayat status for {}", mahasiswa.getNim());
                        riwayat = new RiwayatStatusMahasiswa();
                        riwayat.setMahasiswa(mahasiswa);
                        riwayat.setTahunAkademik(tahunAkademik);
                        riwayat.setCreatedAt(LocalDateTime.now());
                    }

                    if (riwayat != null && !Boolean.TRUE.equals(dto.getPreviewMode())) {
                        riwayat.setStatusBerjalan(dto.getStatusBaru());
                        riwayat.setKeterangan(dto.getKeterangan());
                        riwayat.setTanggalMulai(dto.getTanggalMulai());
                        riwayat.setUpdatedBy(username);
                        riwayat.setUpdatedAt(LocalDateTime.now());
                        results.add(riwayatStatusMahasiswaDao.save(riwayat));

                        // Jika status baru adalah status keluar, update juga mahasiswa
                        if (isStatusKeluar(dto.getStatusBaru())) {
                            StatusKeluar statusKeluar = convertToStatusKeluar(dto.getStatusBaru());
                            if (statusKeluar != null) {
                                mahasiswa.setStatusKeluar(statusKeluar);
                                mahasiswa.setTanggalKeluar(dto.getTanggalKeluar() != null
                                    ? dto.getTanggalKeluar()
                                    : LocalDate.now());
                                if (tahunAkademik != null) {
                                    mahasiswa.setTahunAkademikKeluar(tahunAkademik);
                                }
                                mahasiswa.setStatusKeluarUpdateUser(username);
                                mahasiswa.setStatusKeluarUpdateDate(LocalDateTime.now());
                                mahasiswaDao.save(mahasiswa);
                                log.info("Updated status keluar {} for {} ({})",
                                    statusKeluar, mahasiswa.getNim(), mahasiswa.getNama());
                            }
                        }

                        log.info("Updated status for {} ({})",
                            mahasiswa.getNim(), mahasiswa.getNama());
                    } else if (riwayat != null) {
                        results.add(riwayat);
                        log.info("Preview: Will update status for {} ({})",
                            mahasiswa.getNim(), mahasiswa.getNama());
                    }
                } catch (Exception e) {
                    log.error("Error updating mahasiswa {}: {}", idMahasiswa, e.getMessage());
                    skippedMessages.add("Error updating " + idMahasiswa + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return new BulkUpdateResult(results.size(), skippedMessages.size(), skippedMessages);
        }

        // Jika tidak ada idMahasiswa yang dipilih, gunakan filter untuk bulk update
        Page<Object[]> mahasiswaPage;
        Pageable pageable = Pageable.ofSize(100);

        do {
            mahasiswaPage = riwayatStatusMahasiswaDao.findMahasiswaForBulkUpdate(
                    dto.getIdTahunAkademik(),
                    dto.getIdProdi(),
                    dto.getAngkatan(),
                    dto.getStatusSaatIni() != null ? dto.getStatusSaatIni().name() : null,
                    pageable);

            for (Object[] row : mahasiswaPage.getContent()) {
                String idRiwayat = (String) row[0];
                String idMahasiswa = (String) row[1];
                String nim = (String) row[2];
                String nama = (String) row[3];

                // Jika preview mode, tidak perlu save
                if (Boolean.TRUE.equals(dto.getPreviewMode())) {
                    log.info("Preview: Will update status for {} ({})", nim, nama);
                    continue;
                }

                // Proses update
                RiwayatStatusMahasiswa riwayat = riwayatStatusMahasiswaDao.findById(idRiwayat).orElse(null);
                if (riwayat != null) {
                    riwayat.setStatusBerjalan(dto.getStatusBaru());
                    riwayat.setKeterangan(dto.getKeterangan());
                    riwayat.setTanggalMulai(dto.getTanggalMulai());
                    riwayat.setUpdatedBy(username);
                    riwayat.setUpdatedAt(LocalDateTime.now());
                    results.add(riwayatStatusMahasiswaDao.save(riwayat));

                    // Jika status baru adalah status keluar, update juga mahasiswa
                    if (isStatusKeluar(dto.getStatusBaru())) {
                        Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa).orElse(null);
                        if (mahasiswa != null) {
                            StatusKeluar statusKeluar = convertToStatusKeluar(dto.getStatusBaru());
                            if (statusKeluar != null) {
                                mahasiswa.setStatusKeluar(statusKeluar);
                                mahasiswa.setTanggalKeluar(dto.getTanggalKeluar() != null
                                    ? dto.getTanggalKeluar()
                                    : LocalDate.now());
                                if (riwayat.getTahunAkademik() != null) {
                                    mahasiswa.setTahunAkademikKeluar(riwayat.getTahunAkademik());
                                }
                                mahasiswa.setStatusKeluarUpdateUser(username);
                                mahasiswa.setStatusKeluarUpdateDate(LocalDateTime.now());
                                mahasiswaDao.save(mahasiswa);
                                log.info("Updated status keluar {} for {} ({})",
                                    statusKeluar, nim, nama);
                            }
                        }
                    }

                    log.info("Updated status for {} ({})", nim, nama);
                }
            }

            pageable = mahasiswaPage.nextPageable();

        } while (mahasiswaPage.hasNext());

        return new BulkUpdateResult(results.size(), skippedMessages.size(), skippedMessages);
    }

    /**
     * Auto-create riwayat status untuk mahasiswa yang belum punya
     */
    public void autoCreateRiwayatStatus(String idTahunAkademik, String username) {
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(idTahunAkademik)
                .orElseThrow(() -> new RuntimeException("Tahun akademik tidak ditemukan"));

        // Cari mahasiswa yang belum punya riwayat status
        List<Object[]> mahasiswaList = riwayatStatusMahasiswaDao
                .findMahasiswaWithoutRiwayatStatus(idTahunAkademik);

        log.info("Auto-create riwayat status untuk {} mahasiswa di tahun akademik {}",
                mahasiswaList.size(), tahunAkademik.getNamaTahunAkademik());

        for (Object[] row : mahasiswaList) {
            String idMahasiswa = (String) row[0];
            String nim = (String) row[1];

            try {
                Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa).orElse(null);
                if (mahasiswa != null) {
                    RiwayatStatusMahasiswa riwayat = new RiwayatStatusMahasiswa();
                    riwayat.setMahasiswa(mahasiswa);
                    riwayat.setTahunAkademik(tahunAkademik);
                    riwayat.setStatusBerjalan(StatusRecord.AKTIF);
                    riwayat.setTanggalMulai(LocalDate.now());
                    riwayat.setCreatedBy(username);
                    riwayat.setCreatedAt(LocalDateTime.now());
                    riwayatStatusMahasiswaDao.save(riwayat);
                    log.info("Created riwayat status for {}", nim);
                }
            } catch (Exception e) {
                log.error("Error creating riwayat status for {}: {}", nim, e.getMessage());
            }
        }
    }

    /**
     * Hapus riwayat status
     */
    public void hapusRiwayatStatus(String idRiwayatStatus) {
        riwayatStatusMahasiswaDao.deleteById(idRiwayatStatus);
        log.info("Deleted riwayat status {}", idRiwayatStatus);
    }

    /**
     * Rekap status mahasiswa per tahun akademik
     */
    public List<Object[]> rekapStatusByTahunAkademik(String idTahunAkademik) {
        return riwayatStatusMahasiswaDao.rekapStatusByTahunAkademik(idTahunAkademik);
    }

    /**
     * Rekap status mahasiswa per prodi dan tahun akademik
     */
    public List<Object[]> rekapStatusByProdiAndTahunAkademik(String idTahunAkademik) {
        return riwayatStatusMahasiswaDao.rekapStatusByProdiAndTahunAkademik(idTahunAkademik);
    }

    /**
     * Cari mahasiswa yang cuti lebih dari X semester
     */
    public List<Object[]> findMahasiswaCutiLebihDari(int maxSemester) {
        return riwayatStatusMahasiswaDao.findMahasiswaCutiLebihDari(maxSemester);
    }

    /**
     * Cari mahasiswa yang tidak ada riwayat status di semester aktif
     */
    public List<Object[]> findMahasiswaTanpaRiwayatStatusSemesterAktif() {
        return riwayatStatusMahasiswaDao.findMahasiswaTanpaRiwayatStatusSemesterAktif();
    }

    /**
     * Cari mahasiswa yang status berjalan LULUS tapi belum ada status keluar
     */
    public List<Object[]> findMahasiswaLulusTapiBelumStatusKeluar() {
        return riwayatStatusMahasiswaDao.findMahasiswaLulusTapiBelumStatusKeluar();
    }

    /**
     * Bulk update status keluar menjadi LULUS untuk mahasiswa yang lulus
     */
    public List<Mahasiswa> bulkUpdateStatusKeluarLulus(String username) {
        List<Object[]> mahasiswaList = riwayatStatusMahasiswaDao.findMahasiswaLulusTapiBelumStatusKeluar();
        List<Mahasiswa> results = new ArrayList<>();

        log.info("Bulk update status keluar LULUS untuk {} mahasiswa", mahasiswaList.size());

        for (Object[] row : mahasiswaList) {
            String idMahasiswa = (String) row[0];
            String nim = (String) row[1];
            String nama = (String) row[2];

            try {
                Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa).orElse(null);
                if (mahasiswa != null) {
                    mahasiswa.setStatusKeluar(StatusKeluar.LULUS);
                    mahasiswa.setTanggalKeluar(LocalDate.now());
                    mahasiswa.setStatusKeluarUpdateUser(username);
                    mahasiswa.setStatusKeluarUpdateDate(LocalDateTime.now());
                    mahasiswaDao.save(mahasiswa);
                    results.add(mahasiswa);
                    log.info("Updated status keluar LULUS for {} ({})", nim, nama);
                }
            } catch (Exception e) {
                log.error("Error updating status keluar for {}: {}", nim, e.getMessage());
            }
        }

        return results;
    }

    /**
     * Bulk update status keluar berdasarkan list ID mahasiswa
     * Digunakan untuk update mahasiswa yang dipilih via checkbox
     */
    public BulkUpdateResult bulkUpdateStatusKeluarByIds(List<String> idMahasiswaList,
            StatusKeluar statusKeluar, String username) {
        List<String> skippedMessages = new ArrayList<>();
        int updated = 0;

        log.info("Bulk update status keluar {} untuk {} mahasiswa", statusKeluar, idMahasiswaList.size());

        for (String idMahasiswa : idMahasiswaList) {
            try {
                Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa).orElse(null);
                if (mahasiswa == null) {
                    skippedMessages.add("Mahasiswa tidak ditemukan: " + idMahasiswa);
                    continue;
                }

                // Skip jika sudah ada status keluar
                if (mahasiswa.getStatusKeluar() != null) {
                    skippedMessages.add(mahasiswa.getNim() + " - " + mahasiswa.getNama()
                            + " sudah memiliki status keluar: " + mahasiswa.getStatusKeluar());
                    continue;
                }

                // Update status keluar
                mahasiswa.setStatusKeluar(statusKeluar);
                mahasiswa.setTanggalKeluar(LocalDate.now());
                mahasiswa.setStatusKeluarUpdateUser(username);
                mahasiswa.setStatusKeluarUpdateDate(LocalDateTime.now());
                mahasiswaDao.save(mahasiswa);

                updated++;
                log.info("Updated status keluar {} for {} ({})", statusKeluar,
                        mahasiswa.getNim(), mahasiswa.getNama());

            } catch (Exception e) {
                log.error("Error updating status keluar for {}: {}", idMahasiswa, e.getMessage());
                skippedMessages.add("Error updating " + idMahasiswa + ": " + e.getMessage());
            }
        }

        return new BulkUpdateResult(updated, skippedMessages.size(), skippedMessages);
    }

    // ========================================
    // SMART SUGGESTIONS - Update Status Berjalan
    // ========================================

    /**
     * Ambil data mahasiswa untuk Smart Suggestions dengan status rekomendasi
     */
    public Page<SmartSuggestionDto> getSmartSuggestions(String idProdi, String idTahunAkademik, Pageable pageable) {
        // Jika tahun akademik tidak ditentukan, gunakan tahun akademik aktif
        TahunAkademik tahunAkademik;
        if (idTahunAkademik != null && !idTahunAkademik.isEmpty()) {
            tahunAkademik = tahunAkademikDao.findById(idTahunAkademik).orElse(null);
        } else {
            tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        }

        if (tahunAkademik == null) {
            log.warn("Tahun akademik tidak ditemukan");
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        Page<Object[]> results = riwayatStatusMahasiswaDao.findMahasiswaForSmartSuggestions(idProdi, tahunAkademik.getId(), pageable);
        List<SmartSuggestionDto> suggestions = new ArrayList<>();

        // Cek tanggal mulai kuliah
        boolean sudah4Minggu = false;
        LocalDate taMulai = tahunAkademik.getTanggalMulai();
        LocalDate taSelesai = tahunAkademik.getTanggalSelesai();

        if (tahunAkademik.getTanggalMulaiKuliah() != null) {
            // Cek apakah sudah 4 minggu (28 hari) setelah tanggal mulai kuliah
            LocalDate batas4Minggu = tahunAkademik.getTanggalMulaiKuliah().plusWeeks(4);
            sudah4Minggu = LocalDate.now().isAfter(batas4Minggu);
        }

        for (Object[] row : results.getContent()) {
            SmartSuggestionDto dto = SmartSuggestionDto.fromQueryResult(row);

            // Hitung status rekomendasi dengan parameter tahun akademik
            hitungStatusRekomendasi(dto, sudah4Minggu, taMulai, taSelesai);

            suggestions.add(dto);
        }

        log.info("Smart suggestions: {} mahasiswa untuk tahun akademik {} (page {} of {})",
                suggestions.size(), tahunAkademik.getNamaTahunAkademik(), results.getNumber() + 1, results.getTotalPages());

        return new org.springframework.data.domain.PageImpl<>(suggestions, pageable, results.getTotalElements());
    }

    /**
     * Hitung status rekomendasi berdasarkan kondisi akademik
     * Prioritas:
     * 1. LULUS - jika tanggal_lulus ada dan masuk range tahun akademik
     * 2. PUTUS_STUDI - jika masa studi > batas studi (sesuai jenjang)
     * 3. NONAKTIF - jika sudah cuti 2x dan tidak aktif
     * 4. CUTI - jika semua mk tidak hadir > 3x
     * 5. AKTIF - kondisi normal
     *
     * Batas studi per jenjang (sesuai PDDIKTI):
     * - D3: 10 semester (5 tahun)
     * - S1: 14 semester (7 tahun)
     * - S2: 8 semester (4 tahun)
     * - S3: 12 semester (6 tahun)
     */
    private void hitungStatusRekomendasi(SmartSuggestionDto dto, boolean sudah4Minggu,
            LocalDate taMulai, LocalDate taSelesai) {

        LocalDate tanggalLulus = dto.getTanggalLulus();
        String kodeJenjang = dto.getKodeJenjang();
        Double masaStudiTahun = dto.getMasaStudiTahun();

        // === PRIORITAS 1: LULUS ===
        // Cek apakah tanggal_lulus ada dan masuk range tahun akademik
        if (tanggalLulus != null && taMulai != null && taSelesai != null) {
            if (!tanggalLulus.isBefore(taMulai) && !tanggalLulus.isAfter(taSelesai)) {
                dto.setStatusRekomendasi(StatusRecord.LULUS);
                dto.setKeteranganRekomendasi("Lulus pada " + tanggalLulus.toString());
                return;
            }
        }

        // === PRIORITAS 2: PUTUS_STUDI ===
        // Cek apakah masa studi > batas studi (sesuai jenjang)
        if (masaStudiTahun != null && kodeJenjang != null) {
            double batasStudiTahun = getBatasStudiTahun(kodeJenjang);
            if (masaStudiTahun > batasStudiTahun) {
                dto.setStatusRekomendasi(StatusRecord.PUTUS_STUDI);
                dto.setKeteranganRekomendasi("Masa studi " + masaStudiTahun + " tahun > batas " + batasStudiTahun + " tahun (" + kodeJenjang + ")");
                return;
            }
        }

        // === PRIORITAS 3-5: Status berjalan normal (disederhanakan) ===
        int jumlahCuti = dto.getJumlahCutiSebelumnya() != null ? dto.getJumlahCutiSebelumnya() : 0;
        boolean adaKrs = Boolean.TRUE.equals(dto.getAdaKrs());
        int jumlahDetail = dto.getJumlahKrsDetail() != null ? dto.getJumlahKrsDetail() : 0;

        // Cek apakah sudah cuti 2x atau lebih
        boolean sudahCuti2x = jumlahCuti >= 2;

        // 3. Tidak ada KRS
        if (!adaKrs) {
            if (sudahCuti2x) {
                dto.setStatusRekomendasi(StatusRecord.NONAKTIF);
                dto.setKeteranganRekomendasi("Tidak ada KRS & sudah cuti 2x (butuh perhatian)");
            } else {
                dto.setStatusRekomendasi(StatusRecord.CUTI);
                dto.setKeteranganRekomendasi("Tidak ada KRS (cuti ke-" + (jumlahCuti + 1) + ")");
            }
            return;
        }

        // 4. Ada KRS tapi tidak ada KRS Detail sampai 4 minggu
        if (jumlahDetail == 0 && sudah4Minggu) {
            if (sudahCuti2x) {
                dto.setStatusRekomendasi(StatusRecord.NONAKTIF);
                dto.setKeteranganRekomendasi("KRS kosong sampai minggu ke-4 & sudah cuti 2x (butuh perhatian)");
            } else {
                dto.setStatusRekomendasi(StatusRecord.CUTI);
                dto.setKeteranganRekomendasi("KRS kosong sampai minggu ke-4 (cuti ke-" + (jumlahCuti + 1) + ")");
            }
            return;
        }

        // 5. Kondisi normal - AKTIF (ada KRS)
        dto.setStatusRekomendasi(StatusRecord.AKTIF);
        if (jumlahDetail > 0) {
            dto.setKeteranganRekomendasi("Ada KRS dengan " + jumlahDetail + " mata kuliah");
        } else {
            dto.setKeteranganRekomendasi("Ada KRS (belum ada detail)");
        }
    }

    /**
     * Get batas studi dalam tahun berdasarkan jenjang
     * D3: 5 tahun, S1: 7 tahun, S2: 4 tahun, S3: 6 tahun
     */
    private double getBatasStudiTahun(String kodeJenjang) {
        if (kodeJenjang == null) {
            return 7.0; // Default S1
        }
        switch (kodeJenjang.toUpperCase()) {
            case "D3":
                return 5.0;
            case "S1":
                return 7.0;
            case "S2":
                return 4.0;
            case "S3":
                return 6.0;
            default:
                return 7.0; // Default S1
        }
    }

    /**
     * Update status berjalan untuk mahasiswa yang dipilih
     */
    public BulkUpdateResult updateStatusBerjalanTerpilih(List<String> idMahasiswaList,
            List<String> statusRekomendasiList, String idTahunAkademik, String username) {
        List<String> skippedMessages = new ArrayList<>();
        int updated = 0;

        log.info("Update status berjalan untuk {} mahasiswa", idMahasiswaList.size());
        log.info("idTahunAkademik received: '{}'", idTahunAkademik);

        // Ambil tahun akademik
        TahunAkademik tahunAkademik;
        if (idTahunAkademik != null && !idTahunAkademik.isEmpty()) {
            log.info("Mencari tahun akademik dengan ID: {}", idTahunAkademik);
            tahunAkademik = tahunAkademikDao.findById(idTahunAkademik).orElse(null);
            if (tahunAkademik != null) {
                log.info("Tahun akademik ditemukan: {}", tahunAkademik.getNamaTahunAkademik());
            } else {
                log.warn("Tahun akademik dengan ID {} tidak ditemukan di database", idTahunAkademik);
            }
        } else {
            log.info("idTahunAkademik kosong, mencari tahun akademik aktif");
            tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            if (tahunAkademik != null) {
                log.info("Tahun akademik aktif: {}", tahunAkademik.getNamaTahunAkademik());
            } else {
                log.warn("Tidak ada tahun akademik aktif");
            }
        }

        if (tahunAkademik == null) {
            return new BulkUpdateResult(0, idMahasiswaList.size(),
                    Arrays.asList("Tahun akademik tidak ditemukan"));
        }

        for (int i = 0; i < idMahasiswaList.size(); i++) {
            String idMahasiswa = idMahasiswaList.get(i);
            String statusStr = statusRekomendasiList.get(i);

            try {
                Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa).orElse(null);
                if (mahasiswa == null) {
                    skippedMessages.add("Mahasiswa tidak ditemukan: " + idMahasiswa);
                    continue;
                }

                // Validasi: Cek apakah mahasiswa sudah memiliki status keluar
                if (mahasiswa.getStatusKeluar() != null) {
                    skippedMessages.add(mahasiswa.getNim() + " - " + mahasiswa.getNama() +
                            " sudah memiliki status keluar: " + mahasiswa.getStatusKeluar());
                    continue;
                }

                StatusRecord statusBaru;
                try {
                    statusBaru = StatusRecord.valueOf(statusStr);
                } catch (IllegalArgumentException e) {
                    skippedMessages.add("Status tidak valid: " + statusStr + " untuk " + mahasiswa.getNim());
                    continue;
                }

                // Cari atau buat riwayat status
                Optional<RiwayatStatusMahasiswa> existing = riwayatStatusMahasiswaDao
                        .findByMahasiswaAndTahunAkademik(mahasiswa, tahunAkademik);

                RiwayatStatusMahasiswa riwayat;
                if (existing.isPresent()) {
                    riwayat = existing.get();
                } else {
                    riwayat = new RiwayatStatusMahasiswa();
                    riwayat.setMahasiswa(mahasiswa);
                    riwayat.setTahunAkademik(tahunAkademik);
                    riwayat.setCreatedAt(LocalDateTime.now());
                }

                riwayat.setStatusBerjalan(statusBaru);
                riwayat.setTanggalMulai(LocalDate.now());
                riwayat.setUpdatedBy(username);
                riwayat.setUpdatedAt(LocalDateTime.now());
                riwayatStatusMahasiswaDao.save(riwayat);

                updated++;
                log.info("Updated status berjalan {} for {} ({})", statusBaru,
                        mahasiswa.getNim(), mahasiswa.getNama());

            } catch (Exception e) {
                log.error("Error updating status berjalan for {}: {}", idMahasiswa, e.getMessage());
                skippedMessages.add("Error updating " + idMahasiswa + ": " + e.getMessage());
            }
        }

        return new BulkUpdateResult(updated, skippedMessages.size(), skippedMessages);
    }
}
