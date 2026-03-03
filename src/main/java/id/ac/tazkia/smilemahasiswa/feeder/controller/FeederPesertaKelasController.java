package id.ac.tazkia.smilemahasiswa.feeder.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.tazkia.smilemahasiswa.dao.JadwalDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import id.ac.tazkia.smilemahasiswa.feeder.dto.SyncResult;
import id.ac.tazkia.smilemahasiswa.feeder.service.FeederService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller untuk sinkronisasi Peserta Kelas Kuliah dengan Feeder PDDIKTI.
 * 
 * Prerequisites:
 * - Jadwal harus sudah ter-sync ke Feeder (punya idKelasKuliahFeeder)
 * - Mahasiswa harus sudah terdaftar di Feeder (punya id_registrasi_mahasiswa)
 * 
 * Endpoints:
 * - GET /api/feeder/prepare-insert/peserta-kelas - Prepare daftar peserta untuk
 * sync
 * - POST /api/feeder/insert/peserta-kelas/batch - Batch insert peserta kelas
 */
@Slf4j
@RestController
@RequestMapping("/api/feeder")
@RequiredArgsConstructor
public class FeederPesertaKelasController {

    private final FeederService feederService;
    private final ProdiDao prodiDao;
    private final TahunAkademikDao tahunAkademikDao;
    private final JadwalDao jadwalDao;
    private final KrsDetailDao krsDetailDao;

    /**
     * Prepare insert peserta kelas ke Feeder
     * GET /api/feeder/prepare-insert/peserta-kelas?tahunAkademik=xxx&prodi=yyy
     * 
     * Menampilkan ringkasan peserta per jadwal yang siap untuk sync
     */
    @GetMapping("/prepare-insert/peserta-kelas")
    public ResponseEntity<?> prepareInsertPesertaKelas(
            @RequestParam String tahunAkademik,
            @RequestParam String prodi) {

        log.info("Prepare insert peserta kelas - tahunAkademik: {}, prodi: {}", tahunAkademik, prodi);

        try {
            // Validasi tahun akademik dan prodi
            TahunAkademik ta = tahunAkademikDao.findById(tahunAkademik).orElse(null);
            Prodi p = prodiDao.findById(prodi).orElse(null);

            if (ta == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Tahun akademik tidak ditemukan"));
            }

            if (p == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Prodi tidak ditemukan"));
            }

            // Get jadwal yang sudah ter-sync ke Feeder (punya idKelasKuliahFeeder)
            List<Jadwal> syncedJadwals = jadwalDao
                    .findByTahunAkademikAndProdiAndHariNotNullAndStatus(ta, p, StatusRecord.AKTIF)
                    .stream()
                    .filter(Jadwal::isKelasSyncedToFeeder)
                    .collect(Collectors.toList());

            if (syncedJadwals.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "total", 0,
                        "totalPeserta", 0,
                        "readyToSync", 0,
                        "alreadySynced", 0,
                        "message",
                        "Tidak ada jadwal yang sudah ter-sync ke Feeder. Silakan sync kelas kuliah terlebih dahulu.",
                        "data", List.of()));
            }

            int totalPeserta = 0;
            int readyToSync = 0;
            int alreadySynced = 0;
            int notInFeeder = 0;
            List<Map<String, Object>> jadwalList = new ArrayList<>();

            for (Jadwal jadwal : syncedJadwals) {
                Map<String, Object> info = new HashMap<>();
                info.put("idJadwal", jadwal.getId());
                info.put("idKelasKuliahFeeder", jadwal.getIdKelasKuliahFeeder());

                if (jadwal.getMatakuliahKurikulum() != null &&
                        jadwal.getMatakuliahKurikulum().getMatakuliah() != null) {
                    String kodeMatkul = jadwal.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah();
                    String namaMatkul = jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah();
                    info.put("kodeMatkul", kodeMatkul != null ? kodeMatkul.trim() : "");
                    info.put("namaMatkul", namaMatkul != null ? namaMatkul.trim() : "");
                }

                info.put("namaKelas", jadwal.getKelas() != null ? jadwal.getKelas().getNamaKelas() : "");

                // Get peserta (KrsDetail) for this jadwal
                List<KrsDetail> pesertaList = krsDetailDao.findByJadwalAndStatus(jadwal, StatusRecord.AKTIF);

                int jadwalPeserta = pesertaList.size();
                int jadwalReady = 0;
                int jadwalSynced = 0;
                int jadwalNotInFeeder = 0;

                totalPeserta += jadwalPeserta;

                // Check status masing-masing peserta
                for (KrsDetail krs : pesertaList) {
                    if (krs.getMahasiswa() == null)
                        continue;

                    // Cek apakah sudah di-sync
                    if (krs.isSyncedToFeeder()) {
                        jadwalSynced++;
                        continue;
                    }

                    String nim = krs.getMahasiswa().getNim();
                    if (nim == null || nim.trim().isEmpty()) {
                        jadwalNotInFeeder++;
                        continue;
                    }

                    jadwalReady++;
                }

                readyToSync += jadwalReady;
                alreadySynced += jadwalSynced;
                notInFeeder += jadwalNotInFeeder;

                info.put("totalPeserta", jadwalPeserta);
                info.put("readyToSync", jadwalReady);
                info.put("alreadySynced", jadwalSynced);
                info.put("notInFeeder", jadwalNotInFeeder);
                info.put("canSync", jadwalReady > 0);

                jadwalList.add(info);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "tahunAkademik", Map.of(
                            "id", ta.getId(),
                            "nama", ta.getNamaTahunAkademik()),
                    "prodi", Map.of(
                            "id", p.getId(),
                            "nama", p.getNamaProdi()),
                    "totalJadwal", syncedJadwals.size(),
                    "totalPeserta", totalPeserta,
                    "readyToSync", readyToSync,
                    "alreadySynced", alreadySynced,
                    "notInFeeder", notInFeeder,
                    "data", jadwalList));

        } catch (Exception e) {
            log.error("Error preparing insert peserta kelas", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Batch insert peserta kelas ke Feeder
     * POST /api/feeder/insert/peserta-kelas/batch?tahunAkademik=xxx&prodi=yyy
     * 
     * OPTIMIZED: Batch fetch semua id_registrasi_mahasiswa dan existing peserta di
     * awal
     */
    @PostMapping("/insert/peserta-kelas/batch")
    public ResponseEntity<SyncResult<String>> batchInsertPesertaKelas(
            @RequestParam String tahunAkademik,
            @RequestParam String prodi) {

        log.info("Batch insert peserta kelas - tahunAkademik: {}, prodi: {}", tahunAkademik, prodi);

        try {
            // Validasi tahun akademik dan prodi
            TahunAkademik ta = tahunAkademikDao.findById(tahunAkademik).orElse(null);
            Prodi p = prodiDao.findById(prodi).orElse(null);

            if (ta == null || p == null) {
                return ResponseEntity.badRequest().body(SyncResult.<String>builder()
                        .entityName("PesertaKelas")
                        .build());
            }

            // Get jadwal yang sudah ter-sync ke Feeder
            List<Jadwal> jadwals = jadwalDao
                    .findByTahunAkademikAndProdiAndHariNotNullAndStatus(ta, p, StatusRecord.AKTIF)
                    .stream()
                    .filter(Jadwal::isKelasSyncedToFeeder)
                    .collect(Collectors.toList());

            if (jadwals.isEmpty()) {
                return ResponseEntity.ok(SyncResult.<String>builder()
                        .entityName("PesertaKelas")
                        .matchedCount(0)
                        .mismatchedCount(0)
                        .build());
            }

            // ============================================
            // BATCH OPTIMIZATION: Collect all data upfront
            // ============================================

            // 1. Collect all unique NIMs from ALL jadwals
            List<String> allUniqueNims = new ArrayList<>();
            Map<String, List<KrsDetail>> jadwalPesertaMap = new HashMap<>();

            for (Jadwal jadwal : jadwals) {
                List<KrsDetail> pesertaList = krsDetailDao.findByJadwalAndStatus(jadwal, StatusRecord.AKTIF);
                jadwalPesertaMap.put(jadwal.getId(), pesertaList);

                pesertaList.stream()
                        .filter(krs -> krs.getMahasiswa() != null && !krs.isSyncedToFeeder())
                        .map(krs -> krs.getMahasiswa().getNim())
                        .filter(nim -> nim != null && !nim.trim().isEmpty())
                        .map(String::trim)
                        .forEach(nim -> {
                            if (!allUniqueNims.contains(nim)) {
                                allUniqueNims.add(nim);
                            }
                        });
            }

            // 2. Batch fetch ALL id_registrasi_mahasiswa in ONE API call
            log.info("Batch fetching {} mahasiswa from Feeder", allUniqueNims.size());
            Map<String, String> mahasiswaCache = allUniqueNims.isEmpty()
                    ? new HashMap<>()
                    : feederService.getAllMahasiswaByNims(allUniqueNims);
            log.info("Found {} mahasiswa in Feeder", mahasiswaCache.size());

            // 3. Collect all kelas kuliah IDs and batch fetch existing peserta
            List<String> allKelasIds = jadwals.stream()
                    .map(Jadwal::getIdKelasKuliahFeeder)
                    .collect(Collectors.toList());

            log.info("Batch fetching existing peserta for {} kelas from Feeder", allKelasIds.size());
            Map<String, List<String>> existingPesertaMap = feederService
                    .getExistingPesertaKelasByKelas(allKelasIds);
            log.info("Found existing peserta for {} kelas", existingPesertaMap.size());
            // ============================================

            int successCount = 0;
            int failedCount = 0;
            int skippedCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (Jadwal jadwal : jadwals) {
                String idKelasKuliah = jadwal.getIdKelasKuliahFeeder();
                List<String> existingMahasiswaIds = existingPesertaMap.getOrDefault(idKelasKuliah, new ArrayList<>());

                // Get peserta untuk jadwal ini (dari cache)
                List<KrsDetail> pesertaList = jadwalPesertaMap.get(jadwal.getId());

                for (KrsDetail krs : pesertaList) {
                    try {
                        if (krs.getMahasiswa() == null) {
                            skippedCount++;
                            continue;
                        }

                        // Skip jika sudah di-sync
                        if (krs.isSyncedToFeeder()) {
                            skippedCount++;
                            log.debug("Peserta already synced (local flag) - krs: {}", krs.getId());
                            continue;
                        }

                        String nim = krs.getMahasiswa().getNim();
                        if (nim == null || nim.trim().isEmpty()) {
                            skippedCount++;
                            continue;
                        }

                        nim = nim.trim();

                        // Get id_registrasi_mahasiswa from cache (sudah di-fetch batch di atas)
                        String idRegistrasiMahasiswa = mahasiswaCache.get(nim);

                        if (idRegistrasiMahasiswa == null) {
                            skippedCount++;
                            String errorMsg = String.format("Mahasiswa %s (NIM: %s) tidak ditemukan di Feeder",
                                    krs.getMahasiswa().getNama(), nim);
                            // Only add unique error messages
                            if (!errorMessages.contains(errorMsg)) {
                                errorMessages.add(errorMsg);
                            }
                            continue;
                        }

                        // Check apakah sudah ada di Feeder (dari batch fetch)
                        if (existingMahasiswaIds.contains(idRegistrasiMahasiswa)) {
                            // Update local flag karena sudah ada di Feeder
                            krs.setStatusFeeder(StatusRecord.AKTIF);
                            krsDetailDao.save(krs);
                            skippedCount++;
                            log.debug("Peserta already exists in Feeder - kelas: {}, mhs: {}", idKelasKuliah, nim);
                            continue;
                        }

                        // Insert to Feeder
                        FeederResponse<String> response = feederService.insertPesertaKelasKuliah(
                                idKelasKuliah,
                                idRegistrasiMahasiswa);

                        if (response.isSuccess()) {
                            // Update status di KrsDetail
                            krs.setStatusFeeder(StatusRecord.AKTIF);
                            krsDetailDao.save(krs);

                            successCount++;
                            log.debug("Successfully inserted peserta {} to kelas {}", nim, idKelasKuliah);
                        } else {
                            failedCount++;
                            String errorMsg = String.format("Failed insert peserta %s: %s",
                                    nim, response.getErrorDesc());
                            errorMessages.add(errorMsg);
                            log.error(errorMsg);
                        }

                    } catch (Exception e) {
                        failedCount++;
                        String errorMsg = String.format("Error processing peserta: %s", e.getMessage());
                        errorMessages.add(errorMsg);
                        log.error(errorMsg, e);
                    }
                }
            }

            log.info("Batch insert peserta selesai - success: {}, failed: {}, skipped: {}",
                    successCount, failedCount, skippedCount);

            SyncResult<String> result = SyncResult.<String>builder()
                    .entityName("PesertaKelas")
                    .matchedCount(successCount)
                    .mismatchedCount(failedCount)
                    .missingInFeederCount(skippedCount)
                    .build();

            if (!errorMessages.isEmpty()) {
                // Limit error messages to avoid huge response
                List<String> limitedErrors = errorMessages.size() > 20
                        ? errorMessages.subList(0, 20)
                        : errorMessages;

                List<SyncResult.SyncItem<String>> mismatchedItems = new ArrayList<>();
                for (String errorMsg : limitedErrors) {
                    SyncResult.SyncItem<String> item = new SyncResult.SyncItem<>();
                    item.setDifferences(Arrays.asList(errorMsg));
                    mismatchedItems.add(item);
                }
                result.setMismatched(mismatchedItems);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error batch inserting peserta kelas", e);
            return ResponseEntity.internalServerError().body(SyncResult.<String>builder()
                    .entityName("PesertaKelas")
                    .build());
        }
    }

    /**
     * Insert single jadwal peserta kelas ke Feeder
     * POST /api/feeder/insert/peserta-kelas/single?idJadwal=xxx
     * 
     * OPTIMIZED: Batch fetch semua id_registrasi_mahasiswa di awal
     */
    @PostMapping("/insert/peserta-kelas/single")
    public ResponseEntity<?> insertSinglePesertaKelas(@RequestParam String idJadwal) {
        log.info("Single insert peserta kelas - idJadwal: {}", idJadwal);

        try {
            // Find jadwal
            Jadwal jadwal = jadwalDao.findById(idJadwal).orElse(null);

            if (jadwal == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Jadwal tidak ditemukan: " + idJadwal));
            }

            // Validasi jadwal sudah ter-sync ke Feeder
            if (!jadwal.isKelasSyncedToFeeder()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Jadwal belum ter-sync ke Feeder. Silakan sync kelas kuliah terlebih dahulu."));
            }

            String idKelasKuliah = jadwal.getIdKelasKuliahFeeder();

            // Get peserta untuk jadwal ini
            List<KrsDetail> pesertaList = krsDetailDao.findByJadwalAndStatus(jadwal, StatusRecord.AKTIF);

            if (pesertaList.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Tidak ada peserta untuk jadwal ini"));
            }

            // ============================================
            // BATCH OPTIMIZATION: Collect all unique NIMs first
            // ============================================
            List<String> uniqueNims = pesertaList.stream()
                    .filter(krs -> krs.getMahasiswa() != null && !krs.isSyncedToFeeder())
                    .map(krs -> krs.getMahasiswa().getNim())
                    .filter(nim -> nim != null && !nim.trim().isEmpty())
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());

            // Batch fetch all id_registrasi_mahasiswa in ONE API call
            log.info("Batch fetching {} mahasiswa from Feeder", uniqueNims.size());
            Map<String, String> mahasiswaCache = uniqueNims.isEmpty()
                    ? new HashMap<>()
                    : feederService.getAllMahasiswaByNims(uniqueNims);
            log.info("Found {} mahasiswa in Feeder", mahasiswaCache.size());

            // Also check existing peserta in Feeder for this kelas
            Map<String, List<String>> existingPeserta = feederService
                    .getExistingPesertaKelasByKelas(List.of(idKelasKuliah));
            List<String> existingMahasiswaIds = existingPeserta.getOrDefault(idKelasKuliah, new ArrayList<>());
            log.info("Found {} existing peserta in Feeder for kelas {}", existingMahasiswaIds.size(), idKelasKuliah);
            // ============================================

            int successCount = 0;
            int failedCount = 0;
            int skippedCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (KrsDetail krs : pesertaList) {
                try {
                    if (krs.getMahasiswa() == null) {
                        skippedCount++;
                        continue;
                    }

                    // Skip jika sudah di-sync
                    if (krs.isSyncedToFeeder()) {
                        skippedCount++;
                        log.debug("Peserta already synced (local flag) - krs: {}", krs.getId());
                        continue;
                    }

                    String nim = krs.getMahasiswa().getNim();
                    if (nim == null || nim.trim().isEmpty()) {
                        skippedCount++;
                        continue;
                    }

                    nim = nim.trim();

                    // Get id_registrasi_mahasiswa from cache (sudah di-fetch batch di atas)
                    String idRegistrasiMahasiswa = mahasiswaCache.get(nim);

                    if (idRegistrasiMahasiswa == null) {
                        skippedCount++;
                        String errorMsg = String.format("Mahasiswa %s (NIM: %s) tidak ditemukan di Feeder",
                                krs.getMahasiswa().getNama(), nim);
                        if (!errorMessages.contains(errorMsg)) {
                            errorMessages.add(errorMsg);
                        }
                        continue;
                    }

                    // Check apakah sudah ada di Feeder (dari batch fetch)
                    if (existingMahasiswaIds.contains(idRegistrasiMahasiswa)) {
                        // Update local flag karena sudah ada di Feeder
                        krs.setStatusFeeder(StatusRecord.AKTIF);
                        krsDetailDao.save(krs);
                        skippedCount++;
                        log.debug("Peserta already exists in Feeder - kelas: {}, mhs: {}", idKelasKuliah, nim);
                        continue;
                    }

                    // Insert to Feeder
                    FeederResponse<String> response = feederService.insertPesertaKelasKuliah(
                            idKelasKuliah,
                            idRegistrasiMahasiswa);

                    if (response.isSuccess()) {
                        // Update status di KrsDetail
                        krs.setStatusFeeder(StatusRecord.AKTIF);
                        krsDetailDao.save(krs);

                        successCount++;
                        log.debug("Successfully inserted peserta {} to kelas {}", nim, idKelasKuliah);
                    } else {
                        failedCount++;
                        String errorMsg = String.format("Failed insert peserta %s: %s",
                                nim, response.getErrorDesc());
                        errorMessages.add(errorMsg);
                        log.error(errorMsg);
                    }

                } catch (Exception e) {
                    failedCount++;
                    String errorMsg = String.format("Error processing peserta: %s", e.getMessage());
                    errorMessages.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }

            log.info("Single insert peserta selesai - jadwal: {}, success: {}, failed: {}, skipped: {}",
                    idJadwal, successCount, failedCount, skippedCount);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", failedCount == 0);
            response.put("message", String.format(
                    "Sync peserta kelas selesai. Success: %d, Failed: %d, Skipped: %d",
                    successCount, failedCount, skippedCount));
            response.put("successCount", successCount);
            response.put("failedCount", failedCount);
            response.put("skippedCount", skippedCount);

            if (!errorMessages.isEmpty()) {
                response.put("errors", errorMessages.size() > 10
                        ? errorMessages.subList(0, 10)
                        : errorMessages);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error single inserting peserta kelas", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Verify dan repair peserta kelas yang flag-nya AKTIF tapi ga ada di Feeder
     * POST /api/feeder/verify-repair/peserta-kelas?tahunAkademik=xxx&prodi=yyy
     * 
     * Endpoint ini berguna untuk:
     * - Memverifikasi peserta yang status lokal = AKTIF benar-benar ada di Feeder
     * - Jika tidak ada di Feeder, akan di-insert ulang (repair)
     * - Jika ada di Feeder tapi status lokal bukan AKTIF, akan di-update (sync flag)
     */
    @PostMapping("/verify-repair/peserta-kelas")
    public ResponseEntity<SyncResult<String>> verifyAndRepairPesertaKelas(
            @RequestParam String tahunAkademik,
            @RequestParam String prodi) {

        log.info("Verify & repair peserta kelas - tahunAkademik: {}, prodi: {}", tahunAkademik, prodi);

        try {
            TahunAkademik ta = tahunAkademikDao.findById(tahunAkademik).orElse(null);
            Prodi p = prodiDao.findById(prodi).orElse(null);

            if (ta == null || p == null) {
                return ResponseEntity.badRequest().body(SyncResult.<String>builder()
                        .entityName("PesertaKelas")
                        .errors(List.of("Tahun akademik atau prodi tidak ditemukan"))
                        .build());
            }

            List<Jadwal> jadwals = jadwalDao
                    .findByTahunAkademikAndProdiAndHariNotNullAndStatus(ta, p, StatusRecord.AKTIF)
                    .stream()
                    .filter(Jadwal::isKelasSyncedToFeeder)
                    .collect(Collectors.toList());

            if (jadwals.isEmpty()) {
                return ResponseEntity.ok(SyncResult.<String>builder()
                        .entityName("PesertaKelas")
                        .matchedCount(0)
                        .mismatchedCount(0)
                        .build());
            }

            // Collect all NIMs and build jadwal-peserta map
            List<String> allUniqueNims = new ArrayList<>();
            Map<String, List<KrsDetail>> jadwalPesertaMap = new HashMap<>();

            for (Jadwal jadwal : jadwals) {
                List<KrsDetail> pesertaList = krsDetailDao.findByJadwalAndStatus(jadwal, StatusRecord.AKTIF);
                jadwalPesertaMap.put(jadwal.getId(), pesertaList);

                pesertaList.stream()
                        .filter(krs -> krs.getMahasiswa() != null)
                        .map(krs -> krs.getMahasiswa().getNim())
                        .filter(nim -> nim != null && !nim.trim().isEmpty())
                        .map(String::trim)
                        .forEach(nim -> {
                            if (!allUniqueNims.contains(nim)) {
                                allUniqueNims.add(nim);
                            }
                        });
            }

            // Batch fetch ALL id_registrasi_mahasiswa
            log.info("Batch fetching {} mahasiswa from Feeder", allUniqueNims.size());
            Map<String, String> mahasiswaCache = allUniqueNims.isEmpty()
                    ? new HashMap<>()
                    : feederService.getAllMahasiswaByNims(allUniqueNims);
            log.info("Found {} mahasiswa in Feeder", mahasiswaCache.size());

            // Batch fetch existing peserta from Feeder
            List<String> allKelasIds = jadwals.stream()
                    .map(Jadwal::getIdKelasKuliahFeeder)
                    .collect(Collectors.toList());

            log.info("Batch fetching existing peserta for {} kelas from Feeder", allKelasIds.size());
            Map<String, List<String>> existingPesertaMap = feederService.getExistingPesertaKelasByKelas(allKelasIds);
            log.info("Found existing peserta for {} kelas", existingPesertaMap.size());

            int repairedCount = 0;
            int alreadyOkCount = 0;
            int flagFixedCount = 0;
            int failedCount = 0;
            int notInFeederCount = 0;
            List<String> errorMessages = new ArrayList<>();
            List<String> repairedItems = new ArrayList<>();

            for (Jadwal jadwal : jadwals) {
                String idKelasKuliah = jadwal.getIdKelasKuliahFeeder();
                List<String> existingMahasiswaIds = existingPesertaMap.getOrDefault(idKelasKuliah, new ArrayList<>());

                List<KrsDetail> pesertaList = jadwalPesertaMap.get(jadwal.getId());

                for (KrsDetail krs : pesertaList) {
                    try {
                        if (krs.getMahasiswa() == null) {
                            continue;
                        }

                        String nim = krs.getMahasiswa().getNim();
                        if (nim == null || nim.trim().isEmpty()) {
                            continue;
                        }
                        nim = nim.trim();

                        String idRegistrasiMahasiswa = mahasiswaCache.get(nim);
                        if (idRegistrasiMahasiswa == null) {
                            notInFeederCount++;
                            continue;
                        }

                        boolean localFlagAktif = krs.isSyncedToFeeder();
                        boolean existsInFeeder = existingMahasiswaIds.contains(idRegistrasiMahasiswa);

                        if (existsInFeeder && localFlagAktif) {
                            alreadyOkCount++;
                            continue;
                        }

                        if (existsInFeeder && !localFlagAktif) {
                            krs.setStatusFeeder(StatusRecord.AKTIF);
                            krsDetailDao.save(krs);
                            flagFixedCount++;
                            log.info("Fixed local flag for peserta - kelas: {}, nim: {}", idKelasKuliah, nim);
                            continue;
                        }

                        if (!existsInFeeder) {
                            log.info("Repairing missing peserta - kelas: {}, nim: {}, localFlag: {}", 
                                    idKelasKuliah, nim, localFlagAktif);
                            
                            FeederResponse<String> response = feederService.insertPesertaKelasKuliah(
                                    idKelasKuliah,
                                    idRegistrasiMahasiswa);

                            if (response.isSuccess()) {
                                krs.setStatusFeeder(StatusRecord.AKTIF);
                                krsDetailDao.save(krs);
                                repairedCount++;
                                String item = String.format("%s - %s (%s)", 
                                        jadwal.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah(),
                                        nim, 
                                        krs.getMahasiswa().getNama());
                                repairedItems.add(item);
                                log.info("Repaired peserta - kelas: {}, nim: {}", idKelasKuliah, nim);
                            } else {
                                failedCount++;
                                String errorMsg = String.format("Failed repair peserta %s: %s", nim, response.getErrorDesc());
                                errorMessages.add(errorMsg);
                                log.error(errorMsg);
                            }
                        }

                    } catch (Exception e) {
                        failedCount++;
                        String errorMsg = String.format("Error repairing peserta: %s", e.getMessage());
                        errorMessages.add(errorMsg);
                        log.error(errorMsg, e);
                    }
                }
            }

            log.info("Verify & repair peserta selesai - repaired: {}, alreadyOk: {}, flagFixed: {}, failed: {}, notInFeeder: {}",
                    repairedCount, alreadyOkCount, flagFixedCount, failedCount, notInFeederCount);

            SyncResult<String> result = SyncResult.<String>builder()
                    .entityName("PesertaKelas")
                    .matchedCount(alreadyOkCount)
                    .mismatchedCount(failedCount)
                    .skippedCount(notInFeederCount)
                    .build();

            result.setTotalData(repairedCount + alreadyOkCount + flagFixedCount + failedCount + notInFeederCount);

            if (!repairedItems.isEmpty()) {
                List<SyncResult.SyncItem<String>> repairedSyncItems = new ArrayList<>();
                for (String item : repairedItems.size() > 50 ? repairedItems.subList(0, 50) : repairedItems) {
                    SyncResult.SyncItem<String> syncItem = new SyncResult.SyncItem<>();
                    syncItem.setDifferences(Arrays.asList("REPAIRED: " + item));
                    repairedSyncItems.add(syncItem);
                }
                result.setMissingInFeeder(repairedSyncItems);
            }

            if (!errorMessages.isEmpty()) {
                List<SyncResult.SyncItem<String>> mismatchedItems = new ArrayList<>();
                for (String errorMsg : errorMessages.size() > 20 ? errorMessages.subList(0, 20) : errorMessages) {
                    SyncResult.SyncItem<String> item = new SyncResult.SyncItem<>();
                    item.setDifferences(Arrays.asList(errorMsg));
                    mismatchedItems.add(item);
                }
                result.setMismatched(mismatchedItems);
            }

            // Add summary info
            Map<String, Object> summary = new HashMap<>();
            summary.put("repaired", repairedCount);
            summary.put("alreadyOk", alreadyOkCount);
            summary.put("flagFixed", flagFixedCount);
            summary.put("failed", failedCount);
            summary.put("notInFeeder", notInFeederCount);
            result.setSummary(String.format(
                    "✅ PesertaKelas verified: %d repaired, %d already OK, %d flag fixed, %d failed, %d mhs not in Feeder",
                    repairedCount, alreadyOkCount, flagFixedCount, failedCount, notInFeederCount));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error in verify & repair peserta kelas", e);
            return ResponseEntity.internalServerError().body(SyncResult.<String>builder()
                    .entityName("PesertaKelas")
                    .errors(List.of("Internal server error: " + e.getMessage()))
                    .build());
        }
    }
}
