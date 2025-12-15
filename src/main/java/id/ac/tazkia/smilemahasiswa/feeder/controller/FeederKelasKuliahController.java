package id.ac.tazkia.smilemahasiswa.feeder.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.Akses;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahKurikulum;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import id.ac.tazkia.smilemahasiswa.feeder.dto.SyncResult;
import id.ac.tazkia.smilemahasiswa.feeder.service.FeederService;
import id.ac.tazkia.smilemahasiswa.feeder.util.FeederFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller untuk sinkronisasi data Kelas Kuliah dengan Feeder PDDIKTI.
 * 
 * Endpoints:
 * - GET /api/feeder/prepare-insert/kelas-kuliah - Prepare insert kelas kuliah
 * - POST /api/feeder/insert/kelas-kuliah/batch - Batch insert kelas kuliah
 */
@Slf4j
@RestController
@RequestMapping("/api/feeder")
@RequiredArgsConstructor
public class FeederKelasKuliahController {

    private final FeederService feederService;
    private final ProdiDao prodiDao;
    private final TahunAkademikDao tahunAkademikDao;
    private final JadwalDao jadwalDao;

    /**
     * Prepare insert kelas kuliah ke Feeder
     * GET /api/feeder/prepare-insert/kelas-kuliah?tahunAkademik=xxx&prodi=yyy
     *
     * Response berisi daftar jadwal yang siap untuk sync
     */
    @GetMapping("/prepare-insert/kelas-kuliah")
    public ResponseEntity<?> prepareInsertKelasKuliah(
            @RequestParam String tahunAkademik,
            @RequestParam String prodi) {

        log.info("Prepare insert kelas kuliah - tahunAkademik: {}, prodi: {}", tahunAkademik, prodi);

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

            if (p.getKodePddikti() == null || p.getKodePddikti().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Prodi belum memiliki kode PDDIKTI"));
            }

            // Get all jadwal yang memenuhi kriteria
            List<Jadwal> allJadwal = jadwalDao.findByTahunAkademikAndProdiAndHariNotNullAndStatus(ta, p,
                    StatusRecord.AKTIF);

            if (allJadwal.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "total", 0,
                        "readyToSync", 0,
                        "alreadySynced", 0,
                        "incompleteSchedule", 0,
                        "data", List.of()));
            }

            // Filter jadwal yang sudah sync dan yang belum
            int alreadySynced = 0;
            int incompleteSchedule = 0;
            List<Map<String, Object>> readyToSync = new ArrayList<>();

            for (Jadwal jadwal : allJadwal) {
                Map<String, Object> jadwalInfo = new HashMap<>();
                jadwalInfo.put("idJadwal", jadwal.getId());

                if (jadwal.getMatakuliahKurikulum() != null &&
                        jadwal.getMatakuliahKurikulum().getMatakuliah() != null) {
                    String kodeMatkul = jadwal.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah();
                    String namaMatkul = jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah();
                    // Trim trailing/leading spaces
                    jadwalInfo.put("kodeMatkul", kodeMatkul != null ? kodeMatkul.trim() : "");
                    jadwalInfo.put("namaMatkul", namaMatkul != null ? namaMatkul.trim() : "");
                }
                jadwalInfo.put("namaKelas", jadwal.getKelas() != null ? jadwal.getKelas().getNamaKelas() : "");
                jadwalInfo.put("kapasitas", jadwal.getKapasitas());
                jadwalInfo.put("namaKelasFeeder", jadwal.generateNamaKelasFeeder());

                // Debug info for nama kelas generation
                Map<String, Object> debugInfo = new HashMap<>();
                debugInfo.put("kodeProdi", jadwal.getProdi() != null ? jadwal.getProdi().getKodeProdi() : null);
                debugInfo.put("angkatan", jadwal.getKelas() != null ? jadwal.getKelas().getAngkatan() : null);
                debugInfo.put("hariId", jadwal.getHari() != null ? jadwal.getHari().getId() : null);
                debugInfo.put("hariNama", jadwal.getHari() != null ? jadwal.getHari().getNamaHari() : null);
                jadwalInfo.put("debug", debugInfo);

                // Check apakah sudah sync
                if (jadwal.isKelasSyncedToFeeder()) {
                    alreadySynced++;
                    jadwalInfo.put("statusFeeder", "SYNCED");
                    jadwalInfo.put("idKelasFeeder", jadwal.getIdKelasKuliahFeeder());
                } else {
                    // Check apakah siap untuk sync
                    if (jadwal.isReadyForFeederSync()) {
                        jadwalInfo.put("statusFeeder", "READY_TO_SYNC");
                        jadwalInfo.put("canSync", true);
                        readyToSync.add(jadwalInfo);
                    } else {
                        incompleteSchedule++;
                        jadwalInfo.put("statusFeeder", "INCOMPLETE");
                        jadwalInfo.put("canSync", false);
                        String reason = "Data tidak lengkap: ";
                        if (jadwal.getHari() == null)
                            reason += "hari, ";
                        if (jadwal.getJamMulai() == null)
                            reason += "jam mulai, ";
                        if (jadwal.getJamSelesai() == null)
                            reason += "jam selesai, ";
                        if (jadwal.getRuangan() == null)
                            reason += "ruangan, ";
                        if (jadwal.getKelas() == null)
                            reason += "kelas, ";
                        jadwalInfo.put("reason", reason.substring(0, reason.length() - 2));
                    }
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "tahunAkademik", Map.of(
                            "id", ta.getId(),
                            "nama", ta.getNamaTahunAkademik()),
                    "prodi", Map.of(
                            "id", p.getId(),
                            "nama", p.getNamaProdi(),
                            "kodePddikti", p.getKodePddikti()),
                    "total", allJadwal.size(),
                    "readyToSync", readyToSync.size(),
                    "alreadySynced", alreadySynced,
                    "incompleteSchedule", incompleteSchedule,
                    "data", readyToSync));

        } catch (Exception e) {
            log.error("Error preparing insert kelas kuliah", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Batch insert kelas kuliah ke Feeder
     * POST /api/feeder/insert/kelas-kuliah/batch?tahunAkademik=xxx&prodi=yyy
     */
    @PostMapping("/insert/kelas-kuliah/batch")
    public ResponseEntity<SyncResult<String>> batchInsertKelasKuliah(
            @RequestParam String tahunAkademik,
            @RequestParam String prodi) {

        log.info("Batch insert kelas kuliah - tahunAkademik: {}, prodi: {}", tahunAkademik, prodi);

        try {
            // Validasi tahun akademik dan prodi
            TahunAkademik ta = tahunAkademikDao.findById(tahunAkademik).orElse(null);
            Prodi p = prodiDao.findById(prodi).orElse(null);

            if (ta == null) {
                return ResponseEntity.badRequest().body(SyncResult.<String>builder()
                        .entityName("KelasKuliah")
                        .build());
            }

            if (p == null || p.getKodePddikti() == null || p.getKodePddikti().isEmpty()) {
                return ResponseEntity.badRequest().body(SyncResult.<String>builder()
                        .entityName("KelasKuliah")
                        .build());
            }

            // Get ID prodi dari feeder
            String prodiFilter = FeederFilterBuilder.builder()
                    .eq("kode_program_studi", p.getKodePddikti())
                    .build();
            FeederResponse<Object> prodiResponse = feederService.execute("GetProdi", prodiFilter);
            String idProdiFeeder = extractIdFromResponse(prodiResponse, "id_prodi");

            if (idProdiFeeder == null) {
                return ResponseEntity.badRequest().body(SyncResult.<String>builder()
                        .entityName("KelasKuliah")
                        .build());
            }

            // Get jadwal yang siap untuk sync
            List<Jadwal> jadwals = jadwalDao
                    .findByTahunAkademikAndProdiAndHariNotNullAndStatus(ta, p, StatusRecord.AKTIF)
                    .stream()
                    .filter(j -> !j.isKelasSyncedToFeeder() && j.isReadyForFeederSync())
                    .collect(Collectors.toList());

            if (jadwals.isEmpty()) {
                return ResponseEntity.ok(SyncResult.<String>builder()
                        .entityName("KelasKuliah")
                        .matchedCount(0)
                        .mismatchedCount(0)
                        .build());
            }

            int successCount = 0;
            int failedCount = 0;
            int notFoundCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (Jadwal jadwal : jadwals) {
                try {
                    // Search matakuliah di feeder
                    String kodeMatkul = jadwal.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah();
                    String namaMatkul = jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah();
                    MatakuliahKurikulum mk = jadwal.getMatakuliahKurikulum();

                    Map<String, Object> matkulFeeder = searchMatkulInFeeder(kodeMatkul, namaMatkul, idProdiFeeder);
                    String idMatkulFeeder = null;

                    if (matkulFeeder != null && matkulFeeder.get("idMatkul") != null) {
                        idMatkulFeeder = (String) matkulFeeder.get("idMatkul");
                        log.info("Matakuliah ditemukan di Feeder: {} - {}", kodeMatkul, idMatkulFeeder);
                    } else {
                        // Matakuliah tidak ada, coba insert ke Feeder
                        log.info("Matakuliah not found in Feeder, attempting to insert - kode: {}, nama: {}",
                                kodeMatkul, namaMatkul);

                        try {
                            Map<String, Object> insertResult = insertMatakuliahToFeeder(mk, idProdiFeeder);

                            if (Boolean.TRUE.equals(insertResult.get("success"))) {
                                idMatkulFeeder = (String) insertResult.get("idMatkul");
                                log.info("Auto-insert matakuliah successful: {} -> {}", kodeMatkul, idMatkulFeeder);
                            } else {
                                String errorDetail = (String) insertResult.get("error");
                                log.error("Auto-insert matakuliah failed: {} - {}", kodeMatkul, errorDetail);
                            }
                        } catch (Exception insertEx) {
                            log.error("Auto-insert matakuliah failed: {}", insertEx.getMessage());
                        }
                    }

                    if (idMatkulFeeder == null || idMatkulFeeder.isEmpty()) {
                        notFoundCount++;
                        String errorMsg = String.format(
                                "Matakuliah not found/failed to insert in Feeder - kode: %s, nama: %s", kodeMatkul,
                                namaMatkul);
                        errorMessages.add(errorMsg);
                        log.warn(errorMsg);
                        continue;
                    }

                    // Insert kelas kuliah
                    String namaKelasFeeder = jadwal.generateNamaKelasFeeder();
                    String idSemesterFeeder = ta.getKodeTahunAkademik(); // Misal: "20241"

                    FeederResponse<String> insertResponse = feederService.insertKelasKuliah(
                            idProdiFeeder, idSemesterFeeder, idMatkulFeeder,
                            namaKelasFeeder, jadwal.getKapasitas(), 1, "O");

                    if (insertResponse.isSuccess() && insertResponse.getData() != null) {
                        // Update jadwal dengan ID dari feeder
                        jadwal.setIdKelasKuliahFeeder(insertResponse.getData());
                        jadwal.setStatusKelasFeeder(StatusRecord.AKTIF);
                        jadwalDao.save(jadwal);
                        successCount++;

                        log.info("Successfully inserted kelas kuliah for jadwal: {}", jadwal.getId());
                    } else {
                        failedCount++;
                        String errorMsg = String.format("Failed insert jadwal %s: %s",
                                jadwal.getId(), insertResponse.getErrorDesc());
                        errorMessages.add(errorMsg);
                        log.error(errorMsg);
                    }

                } catch (Exception e) {
                    failedCount++;
                    String errorMsg = String.format("Error processing jadwal %s: %s",
                            jadwal.getId(), e.getMessage());
                    errorMessages.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }

            SyncResult<String> result = SyncResult.<String>builder()
                    .entityName("KelasKuliah")
                    .matchedCount(successCount)
                    .mismatchedCount(failedCount)
                    .missingInFeederCount(notFoundCount)
                    .build();

            // Add error messages to mismatched items
            if (!errorMessages.isEmpty()) {
                List<SyncResult.SyncItem<String>> mismatchedItems = new ArrayList<>();
                for (String errorMsg : errorMessages) {
                    SyncResult.SyncItem<String> item = new SyncResult.SyncItem<>();
                    item.setDifferences(Arrays.asList(errorMsg));
                    mismatchedItems.add(item);
                }
                result.setMismatched(mismatchedItems);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error batch inserting kelas kuliah", e);
            return ResponseEntity.internalServerError().body(SyncResult.<String>builder()
                    .entityName("KelasKuliah")
                    .build());
        }
    }

    /**
     * Single insert kelas kuliah ke Feeder
     * POST /api/feeder/insert/kelas-kuliah/single?jadwalId=xxx
     */
    @PostMapping("/insert/kelas-kuliah/single")
    public ResponseEntity<?> singleInsertKelasKuliah(@RequestParam String jadwalId) {
        log.info("Single insert kelas kuliah - jadwalId: {}", jadwalId);

        try {
            Jadwal jadwal = jadwalDao.findById(jadwalId).orElse(null);
            if (jadwal == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Jadwal tidak ditemukan"));
            }

            // Validasi jadwal belum ter-sync
            if (jadwal.isKelasSyncedToFeeder()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Kelas kuliah sudah ter-sync ke Feeder"));
            }

            // Validasi jadwal siap sync
            if (!jadwal.isReadyForFeederSync()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Jadwal belum siap untuk sync (data belum lengkap)"));
            }

            Prodi p = jadwal.getProdi();
            TahunAkademik ta = jadwal.getTahunAkademik();

            if (p == null || p.getKodePddikti() == null || p.getKodePddikti().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Prodi tidak valid atau belum memiliki kode PDDIKTI"));
            }

            // Get ID prodi dari feeder
            String prodiFilter = FeederFilterBuilder.builder()
                    .eq("kode_program_studi", p.getKodePddikti())
                    .build();
            FeederResponse<Object> prodiResponse = feederService.execute("GetProdi", prodiFilter);
            String idProdiFeeder = extractIdFromResponse(prodiResponse, "id_prodi");

            if (idProdiFeeder == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Prodi tidak ditemukan di Feeder"));
            }

            // Search matakuliah di feeder
            String kodeMatkul = jadwal.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah();
            String namaMatkul = jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah();
            MatakuliahKurikulum mk = jadwal.getMatakuliahKurikulum();

            Map<String, Object> matkulFeeder = searchMatkulInFeeder(kodeMatkul, namaMatkul, idProdiFeeder);
            String idMatkulFeeder = null;

            if (matkulFeeder != null && matkulFeeder.get("idMatkul") != null) {
                idMatkulFeeder = (String) matkulFeeder.get("idMatkul");
                log.info("Matakuliah ditemukan di Feeder: {} - {}", kodeMatkul, idMatkulFeeder);
            } else {
                // Matakuliah tidak ada, coba insert ke Feeder
                log.info("Matakuliah not found in Feeder, attempting to insert");
                Map<String, Object> insertResult = insertMatakuliahToFeeder(mk, idProdiFeeder);

                if (Boolean.TRUE.equals(insertResult.get("success"))) {
                    idMatkulFeeder = (String) insertResult.get("idMatkul");
                    log.info("Auto-insert matakuliah successful: {} -> {}", kodeMatkul, idMatkulFeeder);
                } else {
                    return ResponseEntity.ok(Map.of(
                            "success", false,
                            "message", "Matakuliah tidak ditemukan dan gagal di-insert: " + insertResult.get("error")));
                }
            }

            // Insert kelas kuliah
            String namaKelasFeeder = jadwal.generateNamaKelasFeeder();
            String idSemesterFeeder = ta.getKodeTahunAkademik();

            FeederResponse<String> insertResponse = feederService.insertKelasKuliah(
                    idProdiFeeder, idSemesterFeeder, idMatkulFeeder,
                    namaKelasFeeder, jadwal.getKapasitas(), 1, "O");

            if (insertResponse.isSuccess() && insertResponse.getData() != null) {
                // Update jadwal dengan ID dari feeder
                jadwal.setIdKelasKuliahFeeder(insertResponse.getData());
                jadwal.setStatusKelasFeeder(StatusRecord.AKTIF);
                jadwalDao.save(jadwal);

                log.info("Successfully inserted kelas kuliah for jadwal: {}", jadwal.getId());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Berhasil sync kelas kuliah",
                        "idKelasKuliah", insertResponse.getData()));
            } else {
                log.error("Failed to insert kelas kuliah: {}", insertResponse.getErrorDesc());
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", insertResponse.getErrorDesc() != null ? insertResponse.getErrorDesc()
                                : "Gagal sync ke Feeder"));
            }

        } catch (Exception e) {
            log.error("Error single inserting kelas kuliah", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage()));
        }
    }

    // =====================================================
    // Private Helper Methods
    // =====================================================

    /**
     * Helper: Search matakuliah di Feeder
     * 
     * PENTING: Feeder PDDIKTI menganggap matakuliah dengan kode+nama+sks yang sama
     * sebagai entitas yang SAMA, terlepas dari prodi mana yang mendaftarkannya.
     * 
     * Strategy:
     * 1. Cari dengan filter prodi dulu (ideal case)
     * 2. Jika tidak ditemukan, cari TANPA filter prodi (mungkin sudah ada dari
     * prodi lain)
     */
    private Map<String, Object> searchMatkulInFeeder(String kode, String nama, String idProdi) {
        if (kode == null || kode.isEmpty() || nama == null || nama.isEmpty()) {
            log.warn("Kode atau nama matakuliah kosong - kode: {}, nama: {}", kode, nama);
            return null;
        }

        // Trim trailing/leading spaces dari kode dan nama
        String kodeTrimmed = kode.trim();
        String namaTrimmed = nama.trim();

        // Log warning jika ada spasi yang di-trim
        if (!kode.equals(kodeTrimmed) || !nama.equals(namaTrimmed)) {
            log.warn("Detected spaces in matakuliah - kode: '{}' -> '{}', nama: '{}' -> '{}'",
                    kode, kodeTrimmed, nama, namaTrimmed);
        }

        // Step 1: Cari dengan filter prodi
        String filterWithProdi = FeederFilterBuilder.builder()
                .eq("kode_mata_kuliah", kodeTrimmed)
                .like("nama_mata_kuliah", namaTrimmed)
                .eq("id_prodi", idProdi)
                .build();
        log.info("Searching matakuliah with prodi filter: {}", filterWithProdi);

        FeederResponse<Object> response = feederService.execute("GetListMataKuliah", filterWithProdi);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            if (!dataList.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> first = (Map<String, Object>) dataList.get(0);

                log.info("Matakuliah ditemukan dengan prodi filter - idMatkul: {}", first.get("id_matkul"));
                return Map.of(
                        "idMatkul", first.get("id_matkul"),
                        "matchType", "KODE_NAMA_PRODI",
                        "kode", first.get("kode_mata_kuliah") != null ? first.get("kode_mata_kuliah") : "",
                        "nama", first.get("nama_mata_kuliah") != null ? first.get("nama_mata_kuliah") : "",
                        "sks", first.get("sks_mata_kuliah") != null ? first.get("sks_mata_kuliah") : "",
                        "idProdi", first.get("id_prodi") != null ? first.get("id_prodi") : "");
            }
        }

        // Step 2: Cari TANPA filter prodi (mungkin sudah ada dari prodi lain)
        log.info("Tidak ditemukan dengan prodi filter, mencari tanpa prodi...");
        String filterWithoutProdi = FeederFilterBuilder.builder()
                .eq("kode_mata_kuliah", kodeTrimmed)
                .like("nama_mata_kuliah", namaTrimmed)
                .build();
        log.info("Searching matakuliah WITHOUT prodi filter: {}", filterWithoutProdi);

        FeederResponse<Object> responseGlobal = feederService.execute("GetListMataKuliah", filterWithoutProdi);

        if (responseGlobal.isSuccess() && responseGlobal.getData() != null) {
            List<?> dataList = (List<?>) responseGlobal.getData();
            if (!dataList.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> first = (Map<String, Object>) dataList.get(0);

                log.info(
                        "Matakuliah ditemukan TANPA prodi filter (sudah ada dari prodi lain) - idMatkul: {}, id_prodi: {}",
                        first.get("id_matkul"), first.get("id_prodi"));
                return Map.of(
                        "idMatkul", first.get("id_matkul"),
                        "matchType", "KODE_NAMA_GLOBAL",
                        "kode", first.get("kode_mata_kuliah") != null ? first.get("kode_mata_kuliah") : "",
                        "nama", first.get("nama_mata_kuliah") != null ? first.get("nama_mata_kuliah") : "",
                        "sks", first.get("sks_mata_kuliah") != null ? first.get("sks_mata_kuliah") : "",
                        "idProdi", first.get("id_prodi") != null ? first.get("id_prodi") : "");
            }
        }

        log.info("Matakuliah tidak ditemukan di Feeder - kode: '{}', nama: '{}', prodi: {}", kodeTrimmed, namaTrimmed,
                idProdi);
        return null;
    }

    /**
     * Helper: Insert Matakuliah ke Feeder
     */
    private Map<String, Object> insertMatakuliahToFeeder(MatakuliahKurikulum mk, String idProdiFeeder) {
        String kode = mk.getMatakuliah().getKodeMatakuliah();
        String nama = mk.getMatakuliah().getNamaMatakuliah();
        Integer sksTatapMuka = mk.getJumlahSks();

        // Trim trailing/leading spaces
        String kodeTrimmed = kode != null ? kode.trim() : "";
        String namaTrimmed = nama != null ? nama.trim() : "";

        String idJenisMatkul = null;
        if (mk.getAkses() != null) {
            if (mk.getAkses() == Akses.WAJIB_UMUM) {
                idJenisMatkul = "A";
            } else if (mk.getAkses() == Akses.PILIHAN) {
                idJenisMatkul = "B";
            }
        }

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("kode_mata_kuliah", kodeTrimmed);
        record.put("nama_mata_kuliah", namaTrimmed);
        record.put("id_prodi", idProdiFeeder);
        if (idJenisMatkul != null) {
            record.put("id_jenis_mata_kuliah", idJenisMatkul);
        }

        record.put("sks_tatap_muka", sksTatapMuka != null ? sksTatapMuka : 0);
        record.put("sks_praktek", 0);
        record.put("sks_praktek_lapangan", 0);
        record.put("sks_simulasi", 0);

        log.info("InsertMataKuliah - kode: '{}', nama: '{}', id_prodi: {}, jenis: {}, sks_tatap_muka: {}",
                kodeTrimmed, namaTrimmed, idProdiFeeder, idJenisMatkul, sksTatapMuka);

        FeederResponse<Object> response = feederService.insert("InsertMataKuliah", record);

        if (response.isSuccess()) {
            String idMatkul = extractIdFromResponse(response, "id_matkul");
            return Map.of(
                    "success", true,
                    "idMatkul", idMatkul != null ? idMatkul : "");
        } else {
            return Map.of(
                    "success", false,
                    "error", response.getErrorDesc() != null ? response.getErrorDesc() : "Unknown error");
        }
    }

    /**
     * Helper: Extract ID dari Feeder response
     */
    @SuppressWarnings("unchecked")
    private String extractIdFromResponse(FeederResponse<Object> response, String key) {
        if (response.getData() == null) {
            return null;
        }

        try {
            Object data = response.getData();

            if (data instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) data;
                Object value = dataMap.get(key);
                return value != null ? value.toString() : null;
            } else if (data instanceof List) {
                List<?> dataList = (List<?>) data;
                if (!dataList.isEmpty() && dataList.get(0) instanceof Map) {
                    Map<String, Object> first = (Map<String, Object>) dataList.get(0);
                    Object value = first.get(key);
                    return value != null ? value.toString() : null;
                }
            } else if (data instanceof String) {
                return (String) data;
            }
        } catch (Exception e) {
            log.warn("Error extracting {} from response: {}", key, e.getMessage());
        }

        return null;
    }
}
