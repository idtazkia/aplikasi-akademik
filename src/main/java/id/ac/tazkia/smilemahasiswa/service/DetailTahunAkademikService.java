package id.ac.tazkia.smilemahasiswa.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import id.ac.tazkia.smilemahasiswa.dao.DetailTahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.DetailTahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.Program;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DetailTahunAkademikService {

    @Autowired
    private DetailTahunAkademikDao detailTahunAkademikDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    /**
     * Mendapatkan DetailTahunAkademik yang berlaku untuk mahasiswa tertentu
     * Dengan fallback hierarchy yang cerdas
     * Hasil akan di-cache berdasarkan parameter input
     * 
     * @param tahunAkademik tahun akademik
     * @param mahasiswa     mahasiswa
     * @return DetailTahunAkademik yang berlaku, atau fallback jika tidak ada
     */
    @Cacheable(value = "detailTahunAkademik", key = "#tahunAkademik.id + '_' + #mahasiswa.idProdi.id + '_' + #mahasiswa.idProgram.id + '_' + #mahasiswa.angkatan", unless = "#result == null")
    public DetailTahunAkademik getDetailTahunAkademikForMahasiswa(
            TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {

        String tahunAkademikId = tahunAkademik.getId();
        String prodiId = mahasiswa.getIdProdi().getId();
        String programId = mahasiswa.getIdProgram().getId();
        String angkatan = mahasiswa.getAngkatan();

        // Level 1: Cari rule yang exact match (prodi + program + angkatan)
        List<DetailTahunAkademik> exactRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId, prodiId, programId, angkatan, StatusRecord.AKTIF);

        if (!exactRules.isEmpty()) {
            log.debug("Found exact rule for mahasiswa {} in tahun akademik {}",
                    mahasiswa.getNim(), tahunAkademik.getNamaTahunAkademik());
            return exactRules.get(0);
        }

        // Level 2: Cari rule untuk prodi + NULL program (berlaku untuk semua program di
        // prodi)
        List<DetailTahunAkademik> prodiRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId, prodiId, null, angkatan, StatusRecord.AKTIF);

        if (!prodiRules.isEmpty()) {
            log.info("Using prodi-wide rule for mahasiswa {} (prodi: {})",
                    mahasiswa.getNim(), mahasiswa.getIdProdi().getNamaProdi());
            return prodiRules.get(0);
        }

        // Level 3: Cari rule global untuk program (NULL prodi + program)
        List<DetailTahunAkademik> programRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId, null, programId, angkatan, StatusRecord.AKTIF);

        if (!programRules.isEmpty()) {
            log.info("Using program-wide rule for mahasiswa {} (program: {})",
                    mahasiswa.getNim(), mahasiswa.getIdProgram().getNamaProgram());
            return programRules.get(0);
        }

        // Level 4: Cari rule global (NULL prodi + NULL program)
        List<DetailTahunAkademik> globalRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId, null, null, angkatan, StatusRecord.AKTIF);

        if (!globalRules.isEmpty()) {
            log.info("Using global rule for mahasiswa {} (fallback)", mahasiswa.getNim());
            return globalRules.get(0);
        }

        // Level 5: Fallback ke tahun akademik yang ada data (jika current tahun
        // akademik tidak ada rule)
        log.warn("No rules found for tahun akademik {}. Trying fallback to available tahun akademik...",
                tahunAkademik.getNamaTahunAkademik());

        return getDetailTahunAkademikWithFallbackToAnyTahunAkademik(mahasiswa);
    }

    /**
     * Fallback method: cari DetailTahunAkademik dari tahun akademik mana saja yang
     * ada
     * Prioritas: tahun akademik yang paling baru
     * ULTIMATE FALLBACK: jika tidak ada rule sama sekali, gunakan TahunAkademik
     * langsung
     */
    private DetailTahunAkademik getDetailTahunAkademikWithFallbackToAnyTahunAkademik(Mahasiswa mahasiswa) {
        String prodiId = mahasiswa.getIdProdi().getId();
        String programId = mahasiswa.getIdProgram().getId();
        String angkatan = mahasiswa.getAngkatan();

        // Cari semua DetailTahunAkademik yang match dengan mahasiswa ini, dari tahun
        // akademik apa saja
        List<DetailTahunAkademik> allRules = detailTahunAkademikDao.findAll();

        for (DetailTahunAkademik rule : allRules) {
            if (rule.getStatus() != StatusRecord.AKTIF)
                continue;

            // Check prodi match
            boolean prodiMatch = (rule.getProdi() == null) || rule.getProdi().getId().equals(prodiId);

            // Check program match
            boolean programMatch = (rule.getProgram() == null) || rule.getProgram().getId().equals(programId);

            // Check angkatan range
            boolean angkatanMatch = true;
            if (rule.getAngkatanMulai() != null && angkatan.compareTo(rule.getAngkatanMulai()) < 0) {
                angkatanMatch = false;
            }
            if (rule.getAngkatanSelesai() != null && angkatan.compareTo(rule.getAngkatanSelesai()) > 0) {
                angkatanMatch = false;
            }

            if (prodiMatch && programMatch && angkatanMatch) {
                log.warn("FALLBACK: Using rule from different tahun akademik - {} ({})",
                        rule.getTahunAkademik().getNamaTahunAkademik(),
                        rule.getTahunAkademik().getId());
                return rule;
            }
        }

        // ULTIMATE FALLBACK: Tidak ada rule sama sekali, gunakan TahunAkademik langsung
        log.warn("ULTIMATE FALLBACK: No DetailTahunAkademik rules found, creating virtual rule from TahunAkademik");
        return createVirtualDetailFromTahunAkademik(mahasiswa);
    }

    /**
     * Create virtual DetailTahunAkademik from TahunAkademik data
     * Digunakan sebagai ultimate fallback jika tidak ada rule sama sekali
     */
    private DetailTahunAkademik createVirtualDetailFromTahunAkademik(Mahasiswa mahasiswa) {
        // Cari tahun akademik yang paling relevan (bisa current tahun akademik atau
        // yang aktif)
        TahunAkademik relevantTahunAkademik = findRelevantTahunAkademik();

        if (relevantTahunAkademik == null) {
            log.error("ULTIMATE FALLBACK FAILED: No TahunAkademik found for mahasiswa {}", mahasiswa.getNim());
            return null;
        }

        log.info("Creating virtual DetailTahunAkademik from TahunAkademik: {} for mahasiswa: {}",
                relevantTahunAkademik.getNamaTahunAkademik(), mahasiswa.getNim());

        // Buat DetailTahunAkademik virtual dengan data dari TahunAkademik
        DetailTahunAkademik virtual = new DetailTahunAkademik();
        virtual.setTahunAkademik(relevantTahunAkademik);

        // Copy semua tanggal dari TahunAkademik
        virtual.setTanggalMulaiKrs(relevantTahunAkademik.getTanggalMulaiKrs());
        virtual.setTanggalSelesaiKrs(relevantTahunAkademik.getTanggalSelesaiKrs());
        virtual.setTanggalMulaiKuliah(relevantTahunAkademik.getTanggalMulaiKuliah());
        virtual.setTanggalSelesaiKuliah(relevantTahunAkademik.getTanggalSelesaiKuliah());
        virtual.setTanggalMulaiUts(relevantTahunAkademik.getTanggalMulaiUts());
        virtual.setTanggalSelesaiUts(relevantTahunAkademik.getTanggalSelesaiUts());
        virtual.setTanggalMulaiUas(relevantTahunAkademik.getTanggalMulaiUas());
        virtual.setTanggalSelesaiUas(relevantTahunAkademik.getTanggalSelesaiUas());
        virtual.setTanggalMulaiNilai(relevantTahunAkademik.getTanggalMulaiNilai());
        virtual.setTanggalSelesaiNilai(relevantTahunAkademik.getTanggalSelesaiNilai());

        // Set sebagai rule global (prodi=null, program=null)
        virtual.setProdi(null);
        virtual.setProgram(null);
        virtual.setAngkatanMulai(null);
        virtual.setAngkatanSelesai(null);
        virtual.setPrioritas(0);
        virtual.setStatus(StatusRecord.AKTIF);
        virtual.setKeterangan("Virtual rule from TahunAkademik (Ultimate Fallback)");

        return virtual;
    }

    /**
     * Cari TahunAkademik yang paling relevan untuk digunakan sebagai fallback
     */
    private TahunAkademik findRelevantTahunAkademik() {
        try {
            // Prioritas 1: Cari tahun akademik yang AKTIF
            TahunAkademik aktiveTahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            if (aktiveTahunAkademik != null) {
                log.info("Using AKTIF TahunAkademik for fallback: {}", aktiveTahunAkademik.getNamaTahunAkademik());
                return aktiveTahunAkademik;
            }

            // Prioritas 2: Cari tahun akademik terbaru (berdasarkan kode tahun akademik
            // desc)
            List<TahunAkademik> allTahunAkademik = tahunAkademikDao.findAllByOrderByStatusAscKodeTahunAkademikDesc();
            if (!allTahunAkademik.isEmpty()) {
                TahunAkademik latest = allTahunAkademik.get(0);
                log.info("Using latest TahunAkademik for fallback: {}", latest.getNamaTahunAkademik());
                return latest;
            }

        } catch (Exception e) {
            log.error("Error finding relevant TahunAkademik: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Method overload untuk kemudahan penggunaan
     * 
     * @param tahunAkademikId ID tahun akademik
     * @param prodiId         ID prodi
     * @param programId       ID program
     * @param angkatan        angkatan mahasiswa
     * @return DetailTahunAkademik atau null jika tidak ditemukan
     */
    @Cacheable(value = "detailTahunAkademik", key = "#tahunAkademikId + '_' + #prodiId + '_' + #programId + '_' + #angkatan", unless = "#result == null")
    public DetailTahunAkademik getDetailTahunAkademikForMahasiswa(
            String tahunAkademikId, String prodiId, String programId, String angkatan) {

        log.debug("Mencari DetailTahunAkademik untuk tahunAkademikId: {}, prodiId: {}, programId: {}, angkatan: {}",
                tahunAkademikId, prodiId, programId, angkatan);

        List<DetailTahunAkademik> applicableRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId,
                prodiId,
                programId,
                angkatan,
                StatusRecord.AKTIF);

        log.debug("Ditemukan {} rule yang applicable", applicableRules.size());

        if (!applicableRules.isEmpty()) {
            DetailTahunAkademik selected = applicableRules.get(0);
            log.debug(
                    "Rule terpilih: ID={}, Prodi={}, Program={}, AngkatanMulai={}, AngkatanSelesai={}, Prioritas={}",
                    selected.getId(),
                    selected.getProdi() != null ? selected.getProdi().getId() : "NULL",
                    selected.getProgram() != null ? selected.getProgram().getId() : "NULL",
                    selected.getAngkatanMulai(),
                    selected.getAngkatanSelesai(),
                    selected.getPrioritas());
            return selected;
        }

        // Implement fallback hierarchy untuk method overload juga
        log.info("Exact match not found, trying fallback hierarchy...");

        // Level 2: Prodi-wide rule (program = NULL)
        List<DetailTahunAkademik> prodiRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId, prodiId, null, angkatan, StatusRecord.AKTIF);

        if (!prodiRules.isEmpty()) {
            log.info("Using prodi-wide rule for prodiId: {}", prodiId);
            return prodiRules.get(0);
        }

        // Level 3: Program-wide rule (prodi = NULL)
        List<DetailTahunAkademik> programRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId, null, programId, angkatan, StatusRecord.AKTIF);

        if (!programRules.isEmpty()) {
            log.info("Using program-wide rule for programId: {}", programId);
            return programRules.get(0);
        }

        // Level 4: Global rule (prodi = NULL, program = NULL)
        List<DetailTahunAkademik> globalRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId, null, null, angkatan, StatusRecord.AKTIF);

        if (!globalRules.isEmpty()) {
            log.info("Using global rule for angkatan: {}", angkatan);
            return globalRules.get(0);
        }

        // Level 5: Fallback ke tahun akademik lain
        log.warn("No rules found for tahunAkademikId: {}. Trying fallback to any available tahun akademik...",
                tahunAkademikId);

        DetailTahunAkademik fallbackResult = getDetailTahunAkademikWithFallbackByIds(prodiId, programId, angkatan);
        if (fallbackResult != null) {
            return fallbackResult;
        }

        log.warn(
                "Tidak ditemukan konfigurasi DetailTahunAkademik untuk tahunAkademikId: {}, prodiId: {}, programId: {}, angkatan: {} bahkan dengan fallback",
                tahunAkademikId, prodiId, programId, angkatan);
        return null;
    }

    /**
     * Fallback method untuk versi dengan ID
     * Dengan ultimate fallback ke TahunAkademik
     */
    private DetailTahunAkademik getDetailTahunAkademikWithFallbackByIds(String prodiId, String programId,
            String angkatan) {
        List<DetailTahunAkademik> allRules = detailTahunAkademikDao.findAll();

        for (DetailTahunAkademik rule : allRules) {
            if (rule.getStatus() != StatusRecord.AKTIF)
                continue;

            // Check prodi match
            boolean prodiMatch = (rule.getProdi() == null) || rule.getProdi().getId().equals(prodiId);

            // Check program match
            boolean programMatch = (rule.getProgram() == null) || rule.getProgram().getId().equals(programId);

            // Check angkatan range
            boolean angkatanMatch = true;
            if (rule.getAngkatanMulai() != null && angkatan.compareTo(rule.getAngkatanMulai()) < 0) {
                angkatanMatch = false;
            }
            if (rule.getAngkatanSelesai() != null && angkatan.compareTo(rule.getAngkatanSelesai()) > 0) {
                angkatanMatch = false;
            }

            if (prodiMatch && programMatch && angkatanMatch) {
                log.warn("FALLBACK: Using rule from different tahun akademik - {} ({})",
                        rule.getTahunAkademik().getNamaTahunAkademik(),
                        rule.getTahunAkademik().getId());
                return rule;
            }
        }

        // ULTIMATE FALLBACK: Tidak ada rule sama sekali, gunakan TahunAkademik langsung
        log.warn("ULTIMATE FALLBACK: No DetailTahunAkademik rules found, creating virtual rule from TahunAkademik");
        return createVirtualDetailFromTahunAkademikForIds(prodiId, programId, angkatan);
    }

    /**
     * Create virtual DetailTahunAkademik from TahunAkademik untuk versi ID
     */
    private DetailTahunAkademik createVirtualDetailFromTahunAkademikForIds(String prodiId, String programId,
            String angkatan) {
        TahunAkademik relevantTahunAkademik = findRelevantTahunAkademik();

        if (relevantTahunAkademik == null) {
            log.error(
                    "ULTIMATE FALLBACK FAILED: No TahunAkademik found for prodiId: {}, programId: {}, angkatan: {}",
                    prodiId, programId, angkatan);
            return null;
        }

        log.info(
                "Creating virtual DetailTahunAkademik from TahunAkademik: {} for prodiId: {}, programId: {}, angkatan: {}",
                relevantTahunAkademik.getNamaTahunAkademik(), prodiId, programId, angkatan);

        // Buat DetailTahunAkademik virtual dengan data dari TahunAkademik
        DetailTahunAkademik virtual = new DetailTahunAkademik();
        virtual.setTahunAkademik(relevantTahunAkademik);

        // Copy semua tanggal dari TahunAkademik
        virtual.setTanggalMulaiKrs(relevantTahunAkademik.getTanggalMulaiKrs());
        virtual.setTanggalSelesaiKrs(relevantTahunAkademik.getTanggalSelesaiKrs());
        virtual.setTanggalMulaiKuliah(relevantTahunAkademik.getTanggalMulaiKuliah());
        virtual.setTanggalSelesaiKuliah(relevantTahunAkademik.getTanggalSelesaiKuliah());
        virtual.setTanggalMulaiUts(relevantTahunAkademik.getTanggalMulaiUts());
        virtual.setTanggalSelesaiUts(relevantTahunAkademik.getTanggalSelesaiUts());
        virtual.setTanggalMulaiUas(relevantTahunAkademik.getTanggalMulaiUas());
        virtual.setTanggalSelesaiUas(relevantTahunAkademik.getTanggalSelesaiUas());
        virtual.setTanggalMulaiNilai(relevantTahunAkademik.getTanggalMulaiNilai());
        virtual.setTanggalSelesaiNilai(relevantTahunAkademik.getTanggalSelesaiNilai());

        // Set sebagai rule global (prodi=null, program=null)
        virtual.setProdi(null);
        virtual.setProgram(null);
        virtual.setAngkatanMulai(null);
        virtual.setAngkatanSelesai(null);
        virtual.setPrioritas(0);
        virtual.setStatus(StatusRecord.AKTIF);
        virtual.setKeterangan("Virtual rule from TahunAkademik (Ultimate Fallback)");

        return virtual;
    }

    // ===== VALIDATION METHODS (Lebih praktis untuk kebutuhan validasi) =====

    /**
     * Cek apakah mahasiswa bisa melakukan KRS pada waktu sekarang
     */
    public boolean isKrsPeriodActive(TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {
        DetailTahunAkademik detail = getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);
        if (detail == null)
            return false;

        LocalDate now = LocalDate.now();
        return detail.getTanggalMulaiKrs() != null && detail.getTanggalSelesaiKrs() != null &&
                !now.isBefore(detail.getTanggalMulaiKrs()) && !now.isAfter(detail.getTanggalSelesaiKrs());
    }

    /**
     * Cek apakah mahasiswa bisa melakukan input nilai pada waktu sekarang
     */
    public boolean isInputNilaiPeriodActive(TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {
        DetailTahunAkademik detail = getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);
        if (detail == null)
            return false;

        LocalDate now = LocalDate.now();
        return detail.getTanggalMulaiNilai() != null && detail.getTanggalSelesaiNilai() != null &&
                !now.isBefore(detail.getTanggalMulaiNilai()) && !now.isAfter(detail.getTanggalSelesaiNilai());
    }

    /**
     * Cek apakah sekarang periode kuliah aktif
     */
    public boolean isKuliahPeriodActive(TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {
        DetailTahunAkademik detail = getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);
        if (detail == null)
            return false;

        LocalDate now = LocalDate.now();
        return detail.getTanggalMulaiKuliah() != null && detail.getTanggalSelesaiKuliah() != null &&
                !now.isBefore(detail.getTanggalMulaiKuliah()) && !now.isAfter(detail.getTanggalSelesaiKuliah());
    }

    /**
     * Method paling praktis: langsung return tanggal untuk validasi tanpa entity
     * kompleks
     */
    public LocalDate[] getKrsPeriod(TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {
        log.debug("getKrsPeriod dipanggil untuk tahunAkademik: {}, mahasiswa: {}",
                tahunAkademik.getNamaTahunAkademik(), mahasiswa.getNama());

        DetailTahunAkademik detail = getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);
        if (detail == null) {
            log.warn("getKrsPeriod: DetailTahunAkademik tidak ditemukan untuk tahunAkademik: {}, mahasiswa: {}",
                    tahunAkademik.getNamaTahunAkademik(), mahasiswa.getNama());
            return null;
        }

        LocalDate[] periode = new LocalDate[] { detail.getTanggalMulaiKrs(), detail.getTanggalSelesaiKrs() };
        log.debug("getKrsPeriod: Periode KRS ditemukan - mulai: {}, selesai: {}",
                periode[0], periode[1]);
        return periode;
    }

    /**
     * Return tanggal input nilai
     */
    public LocalDate[] getInputNilaiPeriod(TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {
        DetailTahunAkademik detail = getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);
        if (detail == null)
            return null;

        return new LocalDate[] { detail.getTanggalMulaiNilai(), detail.getTanggalSelesaiNilai() };
    }

    /**
     * Return tanggal UTS
     */
    public LocalDate[] getUtsPeriod(TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {
        DetailTahunAkademik detail = getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);
        if (detail == null)
            return null;

        return new LocalDate[] { detail.getTanggalMulaiUts(), detail.getTanggalSelesaiUts() };
    }

    /**
     * Return tanggal UAS
     */
    public LocalDate[] getUasPeriod(TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {
        DetailTahunAkademik detail = getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);
        if (detail == null)
            return null;

        return new LocalDate[] { detail.getTanggalMulaiUas(), detail.getTanggalSelesaiUas() };
    }

    /**
     * Return study period (periode kuliah) based on schedule's prodi, program, and
     * angkatan
     * This method is specifically designed for jadwal-based queries
     * 
     * @param tahunAkademik tahun akademik from jadwal
     * @param prodi         prodi from jadwal
     * @param program       program from jadwal
     * @param angkatan      angkatan to filter the rules
     * @return LocalDate array [startDate, endDate] of study period, or null if not
     *         found
     */
    public LocalDate[] getStudyPeriod(TahunAkademik tahunAkademik, Prodi prodi, Program program, String angkatan) {
        log.debug("getStudyPeriod dipanggil untuk tahunAkademik: {}, prodi: {}, program: {}, angkatan: {}",
                tahunAkademik != null ? tahunAkademik.getNamaTahunAkademik() : "NULL",
                prodi != null ? prodi.getNamaProdi() : "NULL",
                program != null ? program.getNamaProgram() : "NULL",
                angkatan);

        if (tahunAkademik == null || prodi == null) {
            log.warn("getStudyPeriod: TahunAkademik atau Prodi tidak boleh null");
            return null;
        }

        // Find applicable rule based on prodi, program, and angkatan
        List<DetailTahunAkademik> applicableRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademik.getId(),
                prodi.getId(),
                program != null ? program.getId() : null,
                angkatan, // angkatan parameter
                StatusRecord.AKTIF);

        DetailTahunAkademik detail = null;
        if (!applicableRules.isEmpty()) {
            detail = applicableRules.get(0); // Get the highest priority rule
            log.debug("getStudyPeriod: Rule ditemukan - Prodi: {}, Program: {}, Prioritas: {}",
                    detail.getProdi() != null ? detail.getProdi().getNamaProdi() : "NULL",
                    detail.getProgram() != null ? detail.getProgram().getNamaProgram() : "NULL",
                    detail.getPrioritas());
        } else {
            // Fallback to virtual detail from TahunAkademik if no rule found
            log.warn(
                    "getStudyPeriod: DetailTahunAkademik tidak ditemukan untuk tahunAkademik: {}, prodi: {}, program: {}, angkatan: {}",
                    tahunAkademik.getNamaTahunAkademik(),
                    prodi.getNamaProdi(),
                    program != null ? program.getNamaProgram() : "NULL",
                    angkatan);

            // Create virtual detail from TahunAkademik as fallback
            detail = createVirtualDetailFromTahunAkademikForJadwal(tahunAkademik, prodi, program);
        }

        if (detail == null) {
            log.error("getStudyPeriod: Tidak dapat membuat virtual detail untuk tahunAkademik: {}, prodi: {}",
                    tahunAkademik.getNamaTahunAkademik(), prodi.getNamaProdi());
            return null;
        }

        LocalDate[] periode = new LocalDate[] { detail.getTanggalMulaiKuliah(), detail.getTanggalSelesaiKuliah() };
        log.debug("getStudyPeriod: Periode kuliah ditemukan - mulai: {}, selesai: {}",
                periode[0], periode[1]);
        return periode;
    }

    /**
     * Overloaded method for backward compatibility - calls main method with null
     * angkatan
     * This will find rules that don't have angkatan restrictions
     * 
     * @param tahunAkademik tahun akademik from jadwal
     * @param prodi         prodi from jadwal
     * @param program       program from jadwal
     * @return LocalDate array [startDate, endDate] of study period, or null if not
     *         found
     */
    public LocalDate[] getStudyPeriod(TahunAkademik tahunAkademik, Prodi prodi, Program program) {
        return getStudyPeriod(tahunAkademik, prodi, program, null);
    }

    /**
     * Create virtual DetailTahunAkademik from TahunAkademik data for jadwal-based
     * queries
     * This is used as fallback when no specific rule is found for prodi/program
     * combination
     * 
     * @param tahunAkademik tahun akademik
     * @param prodi         prodi from jadwal
     * @param program       program from jadwal
     * @return virtual DetailTahunAkademik dengan data dari TahunAkademik
     */
    private DetailTahunAkademik createVirtualDetailFromTahunAkademikForJadwal(TahunAkademik tahunAkademik, Prodi prodi,
            Program program) {
        if (tahunAkademik == null) {
            log.error("createVirtualDetailFromTahunAkademikForJadwal: TahunAkademik tidak boleh null");
            return null;
        }

        log.info("Creating virtual DetailTahunAkademik from TahunAkademik: {} for prodi: {}, program: {}",
                tahunAkademik.getNamaTahunAkademik(),
                prodi != null ? prodi.getNamaProdi() : "NULL",
                program != null ? program.getNamaProgram() : "NULL");

        // Buat DetailTahunAkademik virtual dengan data dari TahunAkademik
        DetailTahunAkademik virtual = new DetailTahunAkademik();
        virtual.setTahunAkademik(tahunAkademik);
        virtual.setProdi(prodi);
        virtual.setProgram(program);

        // Copy semua tanggal dari TahunAkademik
        virtual.setTanggalMulaiKrs(tahunAkademik.getTanggalMulaiKrs());
        virtual.setTanggalSelesaiKrs(tahunAkademik.getTanggalSelesaiKrs());
        virtual.setTanggalMulaiKuliah(tahunAkademik.getTanggalMulaiKuliah());
        virtual.setTanggalSelesaiKuliah(tahunAkademik.getTanggalSelesaiKuliah());
        virtual.setTanggalMulaiUts(tahunAkademik.getTanggalMulaiUts());
        virtual.setTanggalSelesaiUts(tahunAkademik.getTanggalSelesaiUts());
        virtual.setTanggalMulaiUas(tahunAkademik.getTanggalMulaiUas());
        virtual.setTanggalSelesaiUas(tahunAkademik.getTanggalSelesaiUas());
        virtual.setTanggalMulaiNilai(tahunAkademik.getTanggalMulaiNilai());
        virtual.setTanggalSelesaiNilai(tahunAkademik.getTanggalSelesaiNilai());

        // Set status aktif untuk virtual rule
        virtual.setStatus(StatusRecord.AKTIF);
        virtual.setPrioritas(999); // Low priority for virtual rule

        log.info("Virtual DetailTahunAkademik created with dates - Kuliah: {} to {}",
                virtual.getTanggalMulaiKuliah(), virtual.getTanggalSelesaiKuliah());

        return virtual;
    }

    /**
     * Cek apakah periode UTS aktif
     */
    public boolean isUtsPeriodActive(TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {
        DetailTahunAkademik detail = getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);
        if (detail == null)
            return false;

        LocalDate now = LocalDate.now();
        return detail.getTanggalMulaiUts() != null && detail.getTanggalSelesaiUts() != null &&
                !now.isBefore(detail.getTanggalMulaiUts()) && !now.isAfter(detail.getTanggalSelesaiUts());
    }

    /**
     * Cek apakah periode UAS aktif
     */
    public boolean isUasPeriodActive(TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {
        DetailTahunAkademik detail = getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);
        if (detail == null)
            return false;

        LocalDate now = LocalDate.now();
        return detail.getTanggalMulaiUas() != null && detail.getTanggalSelesaiUas() != null &&
                !now.isBefore(detail.getTanggalMulaiUas()) && !now.isAfter(detail.getTanggalSelesaiUas());
    }

    /**
     * Clear cache untuk specific mahasiswa dan tahun akademik
     */
    @CacheEvict(value = "detailTahunAkademik", key = "#tahunAkademik.id + '_' + #mahasiswa.idProdi.id + '_' + #mahasiswa.idProgram.id + '_' + #mahasiswa.angkatan")
    public void clearCacheForMahasiswa(TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {
        // Method ini akan clear cache untuk mahasiswa dan tahun akademik tertentu
    }

    /**
     * Clear semua cache DetailTahunAkademik
     * Panggil method ini ketika ada update pada data DetailTahunAkademik
     */
    @CacheEvict(value = "detailTahunAkademik", allEntries = true)
    public void clearAllCache() {
        // Method ini akan clear semua cache DetailTahunAkademik
    }

    /**
     * Method untuk save dengan auto clear cache
     */
    @CacheEvict(value = "detailTahunAkademik", allEntries = true)
    public DetailTahunAkademik save(DetailTahunAkademik detailTahunAkademik) {
        return detailTahunAkademikDao.save(detailTahunAkademik);
    }

    /**
     * Method untuk delete dengan auto clear cache
     */
    @CacheEvict(value = "detailTahunAkademik", allEntries = true)
    public void delete(DetailTahunAkademik detailTahunAkademik) {
        detailTahunAkademikDao.delete(detailTahunAkademik);
    }

    /**
     * Method untuk mendapatkan semua DetailTahunAkademik berdasarkan tahun akademik
     * Juga di-cache karena sering digunakan
     * 
     * @param tahunAkademik tahun akademik
     * @return List DetailTahunAkademik
     */
    @Cacheable(value = "detailTahunAkademikByTahun", key = "#tahunAkademik.id", unless = "#result == null || #result.isEmpty()")
    public List<DetailTahunAkademik> findByTahunAkademik(TahunAkademik tahunAkademik) {
        return detailTahunAkademikDao.findByTahunAkademikAndStatus(tahunAkademik, StatusRecord.AKTIF);
    }

    /**
     * Clear cache untuk tahun akademik tertentu
     */
    @CacheEvict(value = "detailTahunAkademikByTahun", key = "#tahunAkademik.id")
    public void clearCacheForTahunAkademik(TahunAkademik tahunAkademik) {
        // Method ini akan clear cache untuk tahun akademik tertentu
    }

    /**
     * Method helper untuk mendapatkan DetailTahunAkademik dengan fallback ke
     * default
     * Berguna untuk menghindari error ketika tidak ada konfigurasi detail yang
     * spesifik
     * 
     * @param tahunAkademik tahun akademik
     * @param mahasiswa     mahasiswa
     * @return DetailTahunAkademik yang berlaku atau default jika tidak ada yang
     *         spesifik
     */
    public DetailTahunAkademik getDetailTahunAkademikWithFallback(
            TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {

        // Coba ambil yang spesifik dulu
        DetailTahunAkademik detail = getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);

        if (detail != null) {
            return detail;
        }

        // Jika tidak ada yang spesifik, cari yang general (tanpa
        // prodi/program/angkatan)
        List<DetailTahunAkademik> fallbackRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademik.getId(),
                null, // prodi null = berlaku untuk semua prodi
                null, // program null = berlaku untuk semua program
                null, // angkatan null = berlaku untuk semua angkatan
                StatusRecord.AKTIF);

        return fallbackRules.isEmpty() ? null : fallbackRules.get(0);
    }

    /**
     * Method untuk validasi apakah ada konfigurasi detail untuk kombinasi tertentu
     * 
     * @param tahunAkademikId ID tahun akademik
     * @param prodiId         ID prodi
     * @param programId       ID program
     * @param angkatan        angkatan mahasiswa
     * @return true jika ada konfigurasi, false jika tidak ada
     */
    public boolean hasDetailTahunAkademikConfiguration(
            String tahunAkademikId, String prodiId, String programId, String angkatan) {

        List<DetailTahunAkademik> rules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId, prodiId, programId, angkatan, StatusRecord.AKTIF);

        return !rules.isEmpty();
    }

    /**
     * Method untuk debugging - menampilkan semua rule yang match
     * Berguna untuk troubleshooting kenapa suatu mahasiswa tidak mendapat
     * konfigurasi yang tepat
     * 
     * @param tahunAkademikId ID tahun akademik
     * @param prodiId         ID prodi
     * @param programId       ID program
     * @param angkatan        angkatan mahasiswa
     * @return List semua DetailTahunAkademik yang match
     */
    public List<DetailTahunAkademik> debugFindAllMatchingRules(
            String tahunAkademikId, String prodiId, String programId, String angkatan) {

        log.info(
                "DEBUG: Mencari semua matching rules untuk tahunAkademikId: {}, prodiId: {}, programId: {}, angkatan: {}",
                tahunAkademikId, prodiId, programId, angkatan);

        // Debug: Cari info tahun akademik yang sedang dicari
        try {
            TahunAkademik tahunAkademikYangDicari = tahunAkademikDao.findById(tahunAkademikId).orElse(null);
            if (tahunAkademikYangDicari != null) {
                log.info("DEBUG: Tahun akademik yang dicari: {} ({})",
                        tahunAkademikYangDicari.getNamaTahunAkademik(), tahunAkademikId);
            } else {
                log.warn("DEBUG: Tahun akademik dengan ID {} tidak ditemukan!", tahunAkademikId);
            }
        } catch (Exception e) {
            log.warn("DEBUG: Error saat mencari info tahun akademik: {}", e.getMessage());
        }

        // Coba dulu cari semua data untuk tahun akademik ini tanpa filter apapun
        @SuppressWarnings("serial")
        List<DetailTahunAkademik> allDataForTahun = detailTahunAkademikDao.findByTahunAkademikAndStatus(
                new TahunAkademik() {
                    {
                        setId(tahunAkademikId);
                    }
                }, StatusRecord.AKTIF);

        log.info("DEBUG: Total data DetailTahunAkademik untuk tahun akademik {}: {}", tahunAkademikId,
                allDataForTahun.size());

        // Jika tidak ada data sama sekali, coba cari semua data tanpa filter tahun
        // akademik
        if (allDataForTahun.isEmpty()) {
            List<DetailTahunAkademik> allData = detailTahunAkademikDao.findAll();
            log.info("DEBUG: Total semua data DetailTahunAkademik di database: {}", allData.size());

            // Cari tahu tahun akademik apa saja yang tersedia
            Set<String> availableTahunAkademikIds = new HashSet<>();
            for (DetailTahunAkademik data : allData) {
                if (data.getTahunAkademik() != null) {
                    availableTahunAkademikIds.add(data.getTahunAkademik().getId());
                }
                log.info("DEBUG: Semua data - ID: {}, TahunAkademikID: {}, Prodi: {}, Program: {}, Status: {}",
                        data.getId(),
                        data.getTahunAkademik() != null ? data.getTahunAkademik().getId() : "NULL",
                        data.getProdi() != null ? data.getProdi().getId() : "NULL",
                        data.getProgram() != null ? data.getProgram().getId() : "NULL",
                        data.getStatus());
            }

            // Tampilkan semua tahun akademik yang tersedia
            log.info("DEBUG: Tahun akademik yang tersedia di DetailTahunAkademik:");
            for (String taId : availableTahunAkademikIds) {
                try {
                    TahunAkademik ta = tahunAkademikDao.findById(taId).orElse(null);
                    if (ta != null) {
                        log.info("DEBUG: - ID: {}, Nama: {}", taId, ta.getNamaTahunAkademik());
                    }
                } catch (Exception e) {
                    log.warn("DEBUG: Error mengambil info tahun akademik {}: {}", taId, e.getMessage());
                }
            }
        }

        for (DetailTahunAkademik data : allDataForTahun) {
            log.info(
                    "DEBUG: Data tersedia - ID: {}, Prodi: {}, Program: {}, AngkatanMulai: {}, AngkatanSelesai: {}, Prioritas: {}, Status: {}",
                    data.getId(),
                    data.getProdi() != null ? data.getProdi().getId() : "NULL",
                    data.getProgram() != null ? data.getProgram().getId() : "NULL",
                    data.getAngkatanMulai(),
                    data.getAngkatanSelesai(),
                    data.getPrioritas(),
                    data.getStatus());
        }

        List<DetailTahunAkademik> allMatching = detailTahunAkademikDao.findAllMatchingRules(
                tahunAkademikId, prodiId, programId, angkatan, StatusRecord.AKTIF);

        log.info("DEBUG: Ditemukan {} matching rules", allMatching.size());

        // Coba juga dengan query native
        List<Object[]> nativeResults = detailTahunAkademikDao.findMatchingRulesNative(
                tahunAkademikId, prodiId, programId, angkatan, "AKTIF");

        log.info("DEBUG: Query native menemukan {} results", nativeResults.size());

        for (Object[] row : nativeResults) {
            log.info(
                    "DEBUG: Native result - ID: {}, TahunAkademikID: {}, ProdiID: {}, ProgramID: {}, AngkatanMulai: {}, AngkatanSelesai: {}, Prioritas: {}, Status: {}",
                    row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[11]);
        }

        for (int i = 0; i < allMatching.size(); i++) {
            DetailTahunAkademik rule = allMatching.get(i);
            log.info(
                    "DEBUG: Rule #{} - ID: {}, Prodi: {}, Program: {}, AngkatanMulai: {}, AngkatanSelesai: {}, Prioritas: {}",
                    (i + 1),
                    rule.getId(),
                    rule.getProdi() != null ? rule.getProdi().getId() : "NULL",
                    rule.getProgram() != null ? rule.getProgram().getId() : "NULL",
                    rule.getAngkatanMulai(),
                    rule.getAngkatanSelesai(),
                    rule.getPrioritas());
        }

        // Sekarang coba dengan query yang sudah diorder untuk melihat prioritas
        List<DetailTahunAkademik> orderedRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId, prodiId, programId, angkatan, StatusRecord.AKTIF);

        log.info("DEBUG: Setelah ordering, rule terpilih: {}",
                orderedRules.isEmpty() ? "TIDAK ADA"
                        : "ID=" + orderedRules.get(0).getId() + ", Prioritas=" + orderedRules.get(0).getPrioritas());

        return allMatching;
    }
}
