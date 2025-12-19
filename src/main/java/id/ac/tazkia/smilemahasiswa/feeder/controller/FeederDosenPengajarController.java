package id.ac.tazkia.smilemahasiswa.feeder.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.tazkia.smilemahasiswa.dao.JadwalDao;
import id.ac.tazkia.smilemahasiswa.dao.JadwalDosenDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.JadwalDosen;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusJadwalDosen;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import id.ac.tazkia.smilemahasiswa.feeder.dto.SyncResult;
import id.ac.tazkia.smilemahasiswa.feeder.service.FeederService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller untuk sinkronisasi Dosen Pengajar Kelas Kuliah dengan Feeder
 * PDDIKTI.
 * 
 * Prerequisites:
 * - Jadwal harus sudah ter-sync ke Feeder (punya idKelasKuliahFeeder)
 * - Dosen harus punya NIDN yang valid
 * 
 * Endpoints:
 * - GET /api/feeder/prepare-insert/dosen-pengajar - Prepare daftar dosen untuk
 * sync
 * - POST /api/feeder/insert/dosen-pengajar/batch - Batch insert dosen pengajar
 */
@Slf4j
@RestController
@RequestMapping("/api/feeder")
@RequiredArgsConstructor
public class FeederDosenPengajarController {

    private final FeederService feederService;
    private final ProdiDao prodiDao;
    private final TahunAkademikDao tahunAkademikDao;
    private final JadwalDao jadwalDao;
    private final JadwalDosenDao jadwalDosenDao;

    /**
     * Prepare insert dosen pengajar ke Feeder
     * GET /api/feeder/prepare-insert/dosen-pengajar?tahunAkademik=xxx&prodi=yyy
     * 
     * Menampilkan daftar jadwal yang sudah ter-sync beserta status dosen
     * pengajarnya
     * 
     * OPTIMIZED: Fetch semua penugasan dosen dalam 1 API call untuk menghindari N+1
     * problem
     */
    @GetMapping("/prepare-insert/dosen-pengajar")
    public ResponseEntity<?> prepareInsertDosenPengajar(
            @RequestParam String tahunAkademik,
            @RequestParam String prodi) {

        log.info("Prepare insert dosen pengajar - tahunAkademik: {}, prodi: {}", tahunAkademik, prodi);

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
                        "readyToSync", 0,
                        "alreadySynced", 0,
                        "noNidn", 0,
                        "message",
                        "Tidak ada jadwal yang sudah ter-sync ke Feeder. Silakan sync kelas kuliah terlebih dahulu.",
                        "data", List.of()));
            }

            // OPTIMIZATION: Collect semua NIDN unik dari jadwal
            List<String> uniqueNidns = syncedJadwals.stream()
                    .filter(j -> j.getDosen() != null && j.getDosen().getKaryawan() != null)
                    .map(j -> j.getDosen().getKaryawan().getNidn())
                    .filter(nidn -> nidn != null && !nidn.trim().isEmpty())
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());

            // OPTIMIZATION: Fetch semua penugasan dosen dalam 1 API call
            log.info("Fetching {} unique NIDNs from Feeder in batch", uniqueNidns.size());
            Map<String, String> nidnToIdRegistrasi = feederService.getAllPenugasanDosenByNidns(uniqueNidns);
            log.info("Found {} dosen registrations in Feeder", nidnToIdRegistrasi.size());

            // NOTE: Skip check existing dosen pengajar untuk mempercepat loading
            // Jika sudah ada, akan di-handle saat insert (return error dari Feeder)

            int readyToSync = 0;
            int alreadySynced = 0;
            int noNidn = 0;
            int noDosen = 0;
            List<Map<String, Object>> dosenList = new ArrayList<>();

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
                    info.put("sks", jadwal.getMatakuliahKurikulum().getJumlahSks());
                }

                info.put("namaKelas", jadwal.getKelas() != null ? jadwal.getKelas().getNamaKelas() : "");

                // Check apakah sudah di-sync (dari JadwalDosen.statusFeeder)
                JadwalDosen jadwalDosen = jadwalDosenDao.findByJadwalAndStatusJadwalDosen(jadwal,
                        StatusJadwalDosen.PENGAMPU);
                if (jadwalDosen != null && jadwalDosen.isSyncedToFeeder()) {
                    alreadySynced++;
                    info.put("statusDosen", "ALREADY_SYNCED");
                    info.put("canSync", false);
                    info.put("reason", "Sudah ter-sync ke Feeder");

                    // Still add dosen info
                    if (jadwal.getDosen() != null && jadwal.getDosen().getKaryawan() != null) {
                        info.put("namaDosen", jadwal.getDosen().getKaryawan().getNamaKaryawan());
                        info.put("nidn", jadwal.getDosen().getKaryawan().getNidn());
                    }

                    dosenList.add(info);
                    continue;
                }

                // Check dosen
                if (jadwal.getDosen() == null) {
                    noDosen++;
                    info.put("statusDosen", "NO_DOSEN");
                    info.put("canSync", false);
                    info.put("reason", "Jadwal belum memiliki dosen pengampu");
                    dosenList.add(info);
                    continue;
                }

                String namaDosen = jadwal.getDosen().getKaryawan() != null
                        ? jadwal.getDosen().getKaryawan().getNamaKaryawan()
                        : "";
                String nidn = jadwal.getDosen().getKaryawan() != null
                        ? jadwal.getDosen().getKaryawan().getNidn()
                        : null;

                info.put("namaDosen", namaDosen);
                info.put("nidn", nidn);

                if (nidn == null || nidn.trim().isEmpty()) {
                    noNidn++;
                    info.put("statusDosen", "NO_NIDN");
                    info.put("canSync", false);
                    info.put("reason", "Dosen belum memiliki NIDN");
                    dosenList.add(info);
                    continue;
                }

                // Lookup dari cache (bukan API call per dosen!)
                String idRegistrasiDosen = nidnToIdRegistrasi.get(nidn.trim());

                if (idRegistrasiDosen == null) {
                    noNidn++;
                    info.put("statusDosen", "DOSEN_NOT_IN_FEEDER");
                    info.put("canSync", false);
                    info.put("reason", "Dosen tidak ditemukan di Feeder dengan NIDN: " + nidn);
                    dosenList.add(info);
                    continue;
                }

                info.put("idRegistrasiDosen", idRegistrasiDosen);

                // Mark as ready to sync (jika sudah ada di Feeder, akan error saat insert)
                readyToSync++;
                info.put("statusDosen", "READY_TO_SYNC");
                info.put("canSync", true);
                dosenList.add(info);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "tahunAkademik", Map.of(
                            "id", ta.getId(),
                            "nama", ta.getNamaTahunAkademik()),
                    "prodi", Map.of(
                            "id", p.getId(),
                            "nama", p.getNamaProdi()),
                    "total", syncedJadwals.size(),
                    "readyToSync", readyToSync,
                    "alreadySynced", alreadySynced,
                    "noNidn", noNidn,
                    "noDosen", noDosen,
                    "data", dosenList));

        } catch (Exception e) {
            log.error("Error preparing insert dosen pengajar", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Batch insert dosen pengajar ke Feeder
     * POST /api/feeder/insert/dosen-pengajar/batch?tahunAkademik=xxx&prodi=yyy
     * 
     * Optimized with:
     * - Pre-caching id_registrasi_dosen for all unique NIDNs
     * - Parallel processing for faster execution
     */
    @PostMapping("/insert/dosen-pengajar/batch")
    public ResponseEntity<SyncResult<String>> batchInsertDosenPengajar(
            @RequestParam String tahunAkademik,
            @RequestParam String prodi) {

        log.info("Batch insert dosen pengajar - tahunAkademik: {}, prodi: {}", tahunAkademik, prodi);

        try {
            // Validasi tahun akademik dan prodi
            TahunAkademik ta = tahunAkademikDao.findById(tahunAkademik).orElse(null);
            Prodi p = prodiDao.findById(prodi).orElse(null);

            if (ta == null || p == null) {
                return ResponseEntity.badRequest().body(SyncResult.<String>builder()
                        .entityName("DosenPengajar")
                        .build());
            }

            // Get jadwal yang sudah ter-sync ke Feeder dan punya dosen
            List<Jadwal> jadwals = jadwalDao
                    .findByTahunAkademikAndProdiAndHariNotNullAndStatus(ta, p, StatusRecord.AKTIF)
                    .stream()
                    .filter(j -> j.isKelasSyncedToFeeder() && j.getDosen() != null)
                    .collect(Collectors.toList());

            if (jadwals.isEmpty()) {
                return ResponseEntity.ok(SyncResult.<String>builder()
                        .entityName("DosenPengajar")
                        .matchedCount(0)
                        .mismatchedCount(0)
                        .build());
            }

            // Pre-cache: Collect unique NIDNs and warm up cache
            Set<String> uniqueNidns = new HashSet<>();
            for (Jadwal jadwal : jadwals) {
                if (jadwal.getDosen() != null && jadwal.getDosen().getKaryawan() != null) {
                    String nidn = jadwal.getDosen().getKaryawan().getNidn();
                    if (nidn != null && !nidn.trim().isEmpty()) {
                        uniqueNidns.add(nidn.trim());
                    }
                }
            }

            // Pre-fetch id_registrasi_dosen for all unique NIDNs (will be cached)
            log.info("Pre-caching {} unique NIDNs from Feeder", uniqueNidns.size());
            Map<String, String> nidnToIdRegistrasi = new ConcurrentHashMap<>();
            for (String nidn : uniqueNidns) {
                String idRegistrasi = feederService.getIdRegistrasiDosen(nidn);
                if (idRegistrasi != null) {
                    nidnToIdRegistrasi.put(nidn, idRegistrasi);
                }
            }
            log.info("Pre-cached {} dosen from Feeder", nidnToIdRegistrasi.size());

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failedCount = new AtomicInteger(0);
            AtomicInteger skippedCount = new AtomicInteger(0);
            List<String> errorMessages = java.util.Collections.synchronizedList(new ArrayList<>());

            // Process in batches to avoid overwhelming the Feeder API
            int batchSize = 10;
            for (int i = 0; i < jadwals.size(); i += batchSize) {
                List<Jadwal> batch = jadwals.subList(i, Math.min(i + batchSize, jadwals.size()));

                for (Jadwal jadwal : batch) {
                    try {
                        // Skip jika sudah di-sync (cek dari JadwalDosen.statusFeeder)
                        JadwalDosen jadwalDosen = jadwalDosenDao.findByJadwalAndStatusJadwalDosen(jadwal,
                                StatusJadwalDosen.PENGAMPU);
                        if (jadwalDosen != null && jadwalDosen.isSyncedToFeeder()) {
                            skippedCount.incrementAndGet();
                            log.debug("Dosen pengajar already synced (local flag) - jadwal: {}", jadwal.getId());
                            continue;
                        }

                        String nidn = jadwal.getDosen().getKaryawan() != null
                                ? jadwal.getDosen().getKaryawan().getNidn()
                                : null;

                        if (nidn == null || nidn.trim().isEmpty()) {
                            skippedCount.incrementAndGet();
                            continue;
                        }

                        String idKelasKuliah = jadwal.getIdKelasKuliahFeeder();
                        String idRegistrasiDosen = nidnToIdRegistrasi.get(nidn.trim());

                        if (idRegistrasiDosen == null) {
                            skippedCount.incrementAndGet();
                            String errorMsg = String.format("Dosen %s (NIDN: %s) tidak ditemukan di Feeder",
                                    jadwal.getDosen().getKaryawan().getNamaKaryawan(), nidn);
                            errorMessages.add(errorMsg);
                            continue;
                        }

                        // Check apakah sudah ada
                        if (feederService.isDosenPengajarExists(idKelasKuliah, idRegistrasiDosen)) {
                            // Update local status if already exists in Feeder
                            if (jadwalDosen != null && !jadwalDosen.isSyncedToFeeder()) {
                                jadwalDosen.setStatusFeeder(StatusRecord.AKTIF);
                                jadwalDosenDao.save(jadwalDosen);
                            }
                            skippedCount.incrementAndGet();
                            log.debug("Dosen pengajar already exists - kelas: {}, dosen: {}", idKelasKuliah,
                                    idRegistrasiDosen);
                            continue;
                        }

                        // Get SKS from matakuliah
                        Integer sks = jadwal.getMatakuliahKurikulum() != null
                                ? jadwal.getMatakuliahKurikulum().getJumlahSks()
                                : 0;

                        // Insert to Feeder
                        FeederResponse<String> response = feederService.insertDosenPengajarKelasKuliah(
                                idRegistrasiDosen,
                                idKelasKuliah,
                                sks,
                                14, // rencana tatap muka default
                                14, // realisasi tatap muka default
                                1 // id_jenis_evaluasi default
                        );

                        if (response.isSuccess()) {
                            // Update status di JadwalDosen
                            if (jadwalDosen == null) {
                                jadwalDosen = jadwalDosenDao.findByJadwalAndStatusJadwalDosen(jadwal,
                                        StatusJadwalDosen.PENGAMPU);
                            }
                            if (jadwalDosen != null) {
                                jadwalDosen.setAktivitasMengajarFeeder(response.getData());
                                jadwalDosen.setStatusFeeder(StatusRecord.AKTIF);
                                jadwalDosenDao.save(jadwalDosen);
                            }

                            successCount.incrementAndGet();
                            log.info("Successfully inserted dosen pengajar for jadwal: {}", jadwal.getId());
                        } else if (response.getErrorDesc() != null && response.getErrorDesc().toLowerCase().contains("sudah ada")) {
                            // Treat 'Data Dosen Pengajar sudah ada' as success
                            if (jadwalDosen == null) {
                                jadwalDosen = jadwalDosenDao.findByJadwalAndStatusJadwalDosen(jadwal,
                                        StatusJadwalDosen.PENGAMPU);
                            }
                            if (jadwalDosen != null) {
                                jadwalDosen.setStatusFeeder(StatusRecord.AKTIF);
                                // Optionally, clear/set aktivitasMengajarFeeder if you can get the ID from Feeder
                                jadwalDosenDao.save(jadwalDosen);
                            }
                            if (jadwal.getStatusKelasFeeder() != StatusRecord.AKTIF) {
                                jadwal.setStatusKelasFeeder(StatusRecord.AKTIF);
                                jadwalDao.save(jadwal);
                            }
                            successCount.incrementAndGet();
                            log.info("Dosen pengajar already exists in Feeder, updated local status for jadwal: {}", jadwal.getId());
                        } else {
                            failedCount.incrementAndGet();
                            String errorMsg = String.format("Failed insert dosen for jadwal %s: %s",
                                    jadwal.getId(), response.getErrorDesc());
                            errorMessages.add(errorMsg);
                            log.error(errorMsg);
                        }

                    } catch (Exception e) {
                        failedCount.incrementAndGet();
                        String errorMsg = String.format("Error processing jadwal %s: %s",
                                jadwal.getId(), e.getMessage());
                        errorMessages.add(errorMsg);
                        log.error(errorMsg, e);
                    }
                }

                // Log progress for each batch
                log.info("Batch progress: {}/{} jadwals processed", Math.min(i + batchSize, jadwals.size()),
                        jadwals.size());
            }

            SyncResult<String> result = SyncResult.<String>builder()
                    .entityName("DosenPengajar")
                    .matchedCount(successCount.get())
                    .mismatchedCount(failedCount.get())
                    .missingInFeederCount(skippedCount.get())
                    .build();

            if (!errorMessages.isEmpty()) {
                List<SyncResult.SyncItem<String>> mismatchedItems = new ArrayList<>();
                // Limit error messages to avoid huge response
                int maxErrors = Math.min(errorMessages.size(), 50);
                for (int i = 0; i < maxErrors; i++) {
                    SyncResult.SyncItem<String> item = new SyncResult.SyncItem<>();
                    item.setDifferences(Arrays.asList(errorMessages.get(i)));
                    mismatchedItems.add(item);
                }
                if (errorMessages.size() > 50) {
                    SyncResult.SyncItem<String> item = new SyncResult.SyncItem<>();
                    item.setDifferences(Arrays.asList("... dan " + (errorMessages.size() - 50) + " error lainnya"));
                    mismatchedItems.add(item);
                }
                result.setMismatched(mismatchedItems);
            }

            log.info("Batch insert completed - success: {}, failed: {}, skipped: {}",
                    successCount.get(), failedCount.get(), skippedCount.get());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error batch inserting dosen pengajar", e);
            return ResponseEntity.internalServerError().body(SyncResult.<String>builder()
                    .entityName("DosenPengajar")
                    .build());
        }
    }

    /**
     * Single insert dosen pengajar ke Feeder
     * POST /api/feeder/insert/dosen-pengajar/single?jadwalId=xxx
     */
    @PostMapping("/insert/dosen-pengajar/single")
    public ResponseEntity<?> singleInsertDosenPengajar(@RequestParam String jadwalId) {
        log.info("Single insert dosen pengajar - jadwalId: {}", jadwalId);

        try {
            Jadwal jadwal = jadwalDao.findById(jadwalId).orElse(null);
            if (jadwal == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Jadwal tidak ditemukan"));
            }

            // Validasi jadwal sudah ter-sync ke Feeder
            if (!jadwal.isKelasSyncedToFeeder()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Kelas kuliah belum ter-sync ke Feeder"));
            }

            // Validasi dosen
            if (jadwal.getDosen() == null || jadwal.getDosen().getKaryawan() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Jadwal belum memiliki dosen pengampu"));
            }

            String nidn = jadwal.getDosen().getKaryawan().getNidn();
            if (nidn == null || nidn.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Dosen belum memiliki NIDN"));
            }

            // Get id_registrasi_dosen dari Feeder
            String idRegistrasiDosen = feederService.getIdRegistrasiDosen(nidn.trim());
            if (idRegistrasiDosen == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Dosen tidak ditemukan di Feeder dengan NIDN: " + nidn));
            }

            // Get SKS
            Integer sks = 0;
            if (jadwal.getMatakuliahKurikulum() != null) {
                sks = jadwal.getMatakuliahKurikulum().getJumlahSks();
            }

            // Insert ke Feeder
            FeederResponse<String> response = feederService.insertDosenPengajarKelasKuliah(
                    idRegistrasiDosen,
                    jadwal.getIdKelasKuliahFeeder(),
                    sks,
                    14, // rencana tatap muka default
                    14, // realisasi tatap muka default
                    1 // id_jenis_evaluasi default
            );

            if (response.isSuccess()) {
                // Update status di JadwalDosen
                JadwalDosen jadwalDosen = jadwalDosenDao.findByJadwalAndStatusJadwalDosen(jadwal,
                        StatusJadwalDosen.PENGAMPU);
                if (jadwalDosen != null) {
                    jadwalDosen.setAktivitasMengajarFeeder(response.getData());
                    jadwalDosen.setStatusFeeder(StatusRecord.AKTIF);
                    jadwalDosenDao.save(jadwalDosen);
                    log.info("Updated JadwalDosen status feeder for jadwal: {}", jadwalId);
                }

                log.info("Successfully inserted dosen pengajar for jadwal: {}", jadwalId);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Berhasil sync dosen pengajar",
                        "idAktivitasMengajar", response.getData() != null ? response.getData() : ""));
            } else {
                log.error("Failed to insert dosen pengajar: {}", response.getErrorDesc());
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", response.getErrorDesc() != null ? response.getErrorDesc() : "Gagal sync ke Feeder"));
            }

        } catch (Exception e) {
            log.error("Error single inserting dosen pengajar", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage()));
        }
    }
}
