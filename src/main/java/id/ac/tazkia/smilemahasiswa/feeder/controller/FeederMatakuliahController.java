package id.ac.tazkia.smilemahasiswa.feeder.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.tazkia.smilemahasiswa.dao.KurikulumDao;
import id.ac.tazkia.smilemahasiswa.dao.MatakuliahKurikulumDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.entity.Akses;
import id.ac.tazkia.smilemahasiswa.entity.Kurikulum;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahKurikulum;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import id.ac.tazkia.smilemahasiswa.feeder.service.FeederService;
import id.ac.tazkia.smilemahasiswa.feeder.service.KurikulumFeederService;
import id.ac.tazkia.smilemahasiswa.feeder.util.FeederFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller untuk sinkronisasi data Matakuliah dengan Feeder PDDIKTI.
 * 
 * Endpoints:
 * - GET /api/feeder/search/matakuliah - Search matakuliah di Feeder
 * - GET /api/feeder/prepare-insert/matkul-kurikulum - Prepare insert matakuliah
 * kurikulum
 * - POST /api/feeder/insert/matkul-kurikulum - Insert single matakuliah
 * kurikulum
 * - POST /api/feeder/insert/matkul-kurikulum/batch - Batch insert matakuliah
 * kurikulum
 */
@Slf4j
@RestController
@RequestMapping("/api/feeder")
@RequiredArgsConstructor
public class FeederMatakuliahController {

    private final FeederService feederService;
    private final KurikulumFeederService kurikulumFeederService;
    private final ProdiDao prodiDao;
    private final KurikulumDao kurikulumDao;
    private final MatakuliahKurikulumDao matakuliahKurikulumDao;

    /**
     * Search matakuliah di Feeder (GetListMataKuliah)
     * GET /api/feeder/search/matakuliah?kode=MIDU.006&nama=Dirasah&idProdi=xxx
     * 
     * Logika:
     * 1. Cari dengan filter prodi dulu (jika idProdi diisi)
     * 2. Jika tidak ada, cari tanpa prodi dan ambil 1
     */
    @GetMapping("/search/matakuliah")
    public ResponseEntity<?> searchMatakuliah(
            @RequestParam(required = false) String kode,
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) String idProdi) {

        log.info("Searching matakuliah - kode: {}, nama: {}, idProdi: {}", kode, nama, idProdi);

        // Validasi: minimal salah satu harus diisi
        if ((kode == null || kode.isEmpty()) && (nama == null || nama.isEmpty())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Minimal salah satu parameter harus diisi: kode atau nama"));
        }

        try {
            // Build base filter (kode dan/atau nama)
            FeederFilterBuilder baseFilterBuilder = FeederFilterBuilder.builder();

            if (kode != null && !kode.isEmpty()) {
                baseFilterBuilder.eq("kode_mata_kuliah", kode);
            }

            if (nama != null && !nama.isEmpty()) {
                baseFilterBuilder.like("nama_mata_kuliah", nama);
            }

            String baseFilterStr = baseFilterBuilder.build();
            List<?> dataList = null;
            String usedFilter = "";
            String matchType = "";

            // Step 1: Cari dengan prodi dulu (jika idProdi diisi)
            if (idProdi != null && !idProdi.isEmpty()) {
                FeederFilterBuilder filterWithProdiBuilder = FeederFilterBuilder.builder();
                if (kode != null && !kode.isEmpty()) {
                    filterWithProdiBuilder.eq("kode_mata_kuliah", kode);
                }
                if (nama != null && !nama.isEmpty()) {
                    filterWithProdiBuilder.like("nama_mata_kuliah", nama);
                }
                filterWithProdiBuilder.eq("id_prodi", idProdi);
                String filterWithProdi = filterWithProdiBuilder.build();

                log.info("Step 1 - Filter dengan prodi: {}", filterWithProdi);

                FeederResponse<Object> response = feederService.execute("GetListMataKuliah", filterWithProdi);

                if (response.isSuccess() && response.getData() != null) {
                    dataList = (List<?>) response.getData();
                    if (!dataList.isEmpty()) {
                        usedFilter = filterWithProdi;
                        matchType = "DENGAN_PRODI";
                    }
                }
            }

            // Step 2: Jika tidak ada, cari tanpa prodi
            if (dataList == null || dataList.isEmpty()) {
                log.info("Step 2 - Filter tanpa prodi: {}", baseFilterStr);

                FeederResponse<Object> response = feederService.execute("GetListMataKuliah", baseFilterStr);

                if (response.isSuccess() && response.getData() != null) {
                    dataList = (List<?>) response.getData();
                    usedFilter = baseFilterStr;
                    matchType = "TANPA_PRODI";
                } else {
                    return ResponseEntity.ok(Map.of(
                            "success", false,
                            "filter", baseFilterStr,
                            "errorCode", response.getErrorCode(),
                            "errorDesc", response.getErrorDesc() != null ? response.getErrorDesc() : "Unknown error"));
                }
            }

            if (dataList == null || dataList.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "found", false,
                        "filter", usedFilter,
                        "message", "Matakuliah tidak ditemukan di Feeder"));
            }

            // Ambil yang pertama
            @SuppressWarnings("unchecked")
            Map<String, Object> first = (Map<String, Object>) dataList.get(0);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "found", true,
                    "matchType", matchType,
                    "filter", usedFilter,
                    "totalFound", dataList.size(),
                    "selected", Map.of(
                            "id_matkul", first.get("id_matkul") != null ? first.get("id_matkul") : "",
                            "kode_mata_kuliah",
                            first.get("kode_mata_kuliah") != null ? first.get("kode_mata_kuliah") : "",
                            "nama_mata_kuliah",
                            first.get("nama_mata_kuliah") != null ? first.get("nama_mata_kuliah") : "",
                            "sks_mata_kuliah", first.get("sks_mata_kuliah") != null ? first.get("sks_mata_kuliah") : "",
                            "id_prodi", first.get("id_prodi") != null ? first.get("id_prodi") : "",
                            "nama_program_studi",
                            first.get("nama_program_studi") != null ? first.get("nama_program_studi") : ""),
                    "allData", dataList));
        } catch (Exception e) {
            log.error("Error searching matakuliah", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Get matakuliah kurikulum lokal dan cari id_matkul di Feeder
     * GET
     * /api/feeder/prepare-insert/matkul-kurikulum?prodiId=xxx&kurikulumId=yyy&semester=1
     */
    @GetMapping("/prepare-insert/matkul-kurikulum")
    public ResponseEntity<?> prepareInsertMatkulKurikulum(
            @RequestParam String prodiId,
            @RequestParam String kurikulumId,
            @RequestParam(required = false) Integer semester) {

        log.info("Preparing InsertMatkulKurikulum - prodiId: {}, kurikulumId: {}, semester: {}",
                prodiId, kurikulumId, semester);

        // 1. Validasi prodi
        Prodi prodi = prodiDao.findById(prodiId).orElse(null);
        if (prodi == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak ditemukan dengan ID: " + prodiId));
        }

        String kodePddikti = prodi.getKodePddikti();
        if (kodePddikti == null || kodePddikti.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak memiliki kodePddikti: " + prodi.getNamaProdi()));
        }

        // Dapatkan id_prodi dari Feeder (UUID) berdasarkan kode_program_studi
        String idProdiFeeder = kurikulumFeederService.getIdProdiFeeder(prodi);
        if (idProdiFeeder == null || idProdiFeeder.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak ditemukan di Feeder dengan kode: " + kodePddikti));
        }

        // 2. Validasi kurikulum lokal
        Kurikulum kurikulum = kurikulumDao.findById(kurikulumId).orElse(null);
        if (kurikulum == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kurikulum tidak ditemukan dengan ID: " + kurikulumId));
        }

        // Pastikan kurikulum milik prodi yang sama
        if (kurikulum.getProdi() == null || !kurikulum.getProdi().getId().equals(prodiId)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kurikulum tidak terkait dengan Prodi yang dipilih"));
        }

        // 3. Cari id_kurikulum di Feeder
        String idKurikulumFeeder = kurikulumFeederService.getIdKurikulumFeeder(kurikulum);
        if (idKurikulumFeeder == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kurikulum tidak ditemukan di Feeder. Nama: " + kurikulum.getNamaKurikulum()));
        }

        // 4. Get matakuliah kurikulum lokal
        List<MatakuliahKurikulum> lokalList;
        if (semester != null) {
            lokalList = matakuliahKurikulumDao.findByStatusAndKurikulumAndSemester(
                    StatusRecord.AKTIF, kurikulum, semester);
        } else {
            lokalList = matakuliahKurikulumDao.findByStatusAndKurikulumAndSemesterNotNull(
                    StatusRecord.AKTIF, kurikulum);
        }

        if (lokalList.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tidak ada matakuliah kurikulum lokal",
                    "prodi", Map.of(
                            "id", prodi.getId(),
                            "nama", prodi.getNamaProdi(),
                            "kodePddikti", kodePddikti,
                            "idProdiFeeder", idProdiFeeder),
                    "kurikulum", Map.of(
                            "idLokal", kurikulum.getId(),
                            "nama", kurikulum.getNamaKurikulum(),
                            "idFeeder", idKurikulumFeeder),
                    "data", List.of()));
        }

        // 5. Untuk setiap matakuliah, cari id_matkul di Feeder
        List<Map<String, Object>> results = new ArrayList<>();
        int foundCount = 0;
        int notFoundCount = 0;
        int alreadyInsertedCount = 0;

        for (MatakuliahKurikulum mk : lokalList) {
            String kodeLokal = mk.getMatakuliah().getKodeMatakuliah();
            String namaLokal = mk.getMatakuliah().getNamaMatakuliah();

            Map<String, Object> item = new LinkedHashMap<>();

            // Flag: sudah di-insert ke Feeder atau belum
            boolean alreadyInserted = mk.isSyncedToFeeder();
            item.put("idMatakuliahKurikulum", mk.getId());
            item.put("alreadyInsertedToFeeder", alreadyInserted);
            if (alreadyInserted) {
                item.put("statusFeeder", mk.getStatusFeeder().name());
                alreadyInsertedCount++;
            }

            // Search di Feeder - gunakan idProdiFeeder (UUID), bukan kodePddikti
            Map<String, Object> feederResult = searchMatkulInFeeder(kodeLokal, namaLokal, idProdiFeeder);

            if (feederResult != null && feederResult.get("idMatkul") != null) {
                foundCount++;
                item.put("foundInFeeder", true);
                item.put("kodeLokal", kodeLokal != null ? kodeLokal : "");
                item.put("namaLokal", namaLokal);
                item.put("matkulFeeder", Map.of(
                        "idMatkul", feederResult.get("idMatkul"),
                        "kode", feederResult.get("kode") != null ? feederResult.get("kode") : "",
                        "nama", feederResult.get("nama") != null ? feederResult.get("nama") : "",
                        "sks", feederResult.get("sks") != null ? feederResult.get("sks") : ""));
                item.put("semester", mk.getSemester());
                item.put("sksLokal", mk.getJumlahSks());
                item.put("message",
                        alreadyInserted ? "Sudah di-insert ke Feeder" : "Match type: " + feederResult.get("matchType"));

                // Data siap untuk InsertMatkulKurikulum (hanya jika belum di-insert)
                if (!alreadyInserted) {
                    item.put("insertData", Map.of(
                            "id_kurikulum", idKurikulumFeeder,
                            "id_matkul", feederResult.get("idMatkul"),
                            "semester", mk.getSemester()));
                }
            } else {
                notFoundCount++;
                item.put("foundInFeeder", false);
                item.put("kodeLokal", kodeLokal != null ? kodeLokal : "");
                item.put("namaLokal", namaLokal);
                item.put("matkulFeeder", "");
                item.put("semester", mk.getSemester());
                item.put("sksLokal", mk.getJumlahSks());
                item.put("message", "Tidak ditemukan di Feeder");
            }

            results.add(item);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "prodi", Map.of(
                        "id", prodi.getId(),
                        "nama", prodi.getNamaProdi(),
                        "kodePddikti", kodePddikti,
                        "idProdiFeeder", idProdiFeeder),
                "kurikulum", Map.of(
                        "idLokal", kurikulum.getId(),
                        "nama", kurikulum.getNamaKurikulum(),
                        "idFeeder", idKurikulumFeeder),
                "semester", semester != null ? semester : "ALL",
                "summary", Map.of(
                        "total", lokalList.size(),
                        "found", foundCount,
                        "notFound", notFoundCount,
                        "alreadyInserted", alreadyInsertedCount,
                        "readyToInsert", foundCount - alreadyInsertedCount),
                "data", results));
    }

    /**
     * Insert single MatakuliahKurikulum ke Feeder
     * POST /api/feeder/insert/matkul-kurikulum?id=xxx
     */
    @PostMapping("/insert/matkul-kurikulum")
    public ResponseEntity<?> insertMatkulKurikulum(@RequestParam String id) {
        log.info("Insert MatkulKurikulum to Feeder - id: {}", id);

        // 1. Get MatakuliahKurikulum lokal
        MatakuliahKurikulum mk = matakuliahKurikulumDao.findById(id).orElse(null);
        if (mk == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "MatakuliahKurikulum tidak ditemukan dengan ID: " + id));
        }

        // Cek apakah sudah di-sync
        if (mk.isSyncedToFeeder()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "MatakuliahKurikulum sudah di-sync ke Feeder"));
        }

        // Cek apakah SKS 0 (tidak perlu di-sync)
        if (mk.getJumlahSks() == null || mk.getJumlahSks() == 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "MatakuliahKurikulum dengan SKS 0 tidak perlu di-sync ke Feeder"));
        }

        // 2. Validasi kurikulum & prodi
        Kurikulum kurikulum = mk.getKurikulum();
        if (kurikulum == null || kurikulum.getIdPddikti() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kurikulum tidak valid atau belum memiliki idPddikti"));
        }

        Prodi prodi = kurikulum.getProdi();
        if (prodi == null || prodi.getKodePddikti() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak valid atau belum memiliki kodePddikti"));
        }

        // Get id_prodi dari Feeder (UUID)
        String idProdiFeeder = kurikulumFeederService.getIdProdiFeeder(prodi);
        if (idProdiFeeder == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak ditemukan di Feeder: " + prodi.getKodePddikti()));
        }

        String idKurikulumFeeder = kurikulum.getIdPddikti();
        String kodeLokal = mk.getMatakuliah().getKodeMatakuliah();
        String namaLokal = mk.getMatakuliah().getNamaMatakuliah();

        // 3. Search matakuliah di Feeder
        Map<String, Object> feederMatkul = searchMatkulInFeeder(kodeLokal, namaLokal, idProdiFeeder);
        String idMatkulFeeder = null;
        boolean matkulInserted = false;
        Map<String, Object> insertMatkulResult = null;

        if (feederMatkul != null && feederMatkul.get("idMatkul") != null) {
            // Matakuliah sudah ada di Feeder
            idMatkulFeeder = (String) feederMatkul.get("idMatkul");
            log.info("Matakuliah ditemukan di Feeder: {} - {}", kodeLokal, idMatkulFeeder);
        } else {
            // 4. Insert Matakuliah ke Feeder
            log.info("Matakuliah tidak ditemukan, insert ke Feeder: {}", kodeLokal);
            insertMatkulResult = insertMatakuliahToFeeder(mk, idProdiFeeder);

            if (!(Boolean) insertMatkulResult.get("success")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Gagal insert Matakuliah ke Feeder",
                        "error", insertMatkulResult.get("error")));
            }

            idMatkulFeeder = (String) insertMatkulResult.get("idMatkul");
            matkulInserted = true;
        }

        // 5. Insert MatkulKurikulum ke Feeder
        Map<String, Object> insertMkResult = insertMatkulKurikulumToFeeder(
                idKurikulumFeeder, idMatkulFeeder, mk.getSemester(), mk.getAkses());

        if (!(Boolean) insertMkResult.get("success")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Gagal insert MatkulKurikulum ke Feeder",
                    "error", insertMkResult.get("error"),
                    "matkulInserted", matkulInserted,
                    "idMatkulFeeder", idMatkulFeeder));
        }

        boolean alreadyExistsInFeeder = Boolean.TRUE.equals(insertMkResult.get("alreadyExists"));

        // 6. Update statusFeeder di lokal
        mk.setStatusFeeder(StatusRecord.AKTIF);
        matakuliahKurikulumDao.save(mk);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        if (alreadyExistsInFeeder) {
            response.put("message", "Data sudah ada di Feeder, status lokal diupdate menjadi AKTIF");
        } else {
            response.put("message", "Berhasil insert MatkulKurikulum ke Feeder");
        }
        response.put("alreadyExistsInFeeder", alreadyExistsInFeeder);
        response.put("matakuliahKurikulum", Map.of(
                "idLokal", mk.getId(),
                "statusFeeder", "AKTIF",
                "kode", kodeLokal != null ? kodeLokal : "",
                "nama", namaLokal,
                "semester", mk.getSemester()));
        response.put("matkulInserted", matkulInserted);
        if (matkulInserted && insertMatkulResult != null) {
            response.put("insertedMatkul", Map.of(
                    "idMatkul", idMatkulFeeder,
                    "kode", kodeLokal != null ? kodeLokal : "",
                    "nama", namaLokal));
        } else {
            response.put("existingMatkul", Map.of(
                    "idMatkul", idMatkulFeeder,
                    "matchType", feederMatkul != null ? feederMatkul.get("matchType") : "UNKNOWN"));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Batch insert MatkulKurikulum ke Feeder
     * POST /api/feeder/insert/matkul-kurikulum/batch?kurikulumId=xxx&semester=1
     */
    @PostMapping("/insert/matkul-kurikulum/batch")
    public ResponseEntity<?> insertMatkulKurikulumBatch(
            @RequestParam String kurikulumId,
            @RequestParam(required = false) Integer semester) {

        log.info("Batch insert MatkulKurikulum - kurikulumId: {}, semester: {}", kurikulumId, semester);

        // 1. Validasi kurikulum
        Kurikulum kurikulum = kurikulumDao.findById(kurikulumId).orElse(null);
        if (kurikulum == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kurikulum tidak ditemukan dengan ID: " + kurikulumId));
        }

        if (kurikulum.getIdPddikti() == null || kurikulum.getIdPddikti().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kurikulum belum memiliki idPddikti"));
        }

        Prodi prodi = kurikulum.getProdi();
        String idProdiFeeder = kurikulumFeederService.getIdProdiFeeder(prodi);
        if (idProdiFeeder == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak ditemukan di Feeder: " + prodi.getKodePddikti()));
        }

        String idKurikulumFeeder = kurikulum.getIdPddikti();

        // 2. Get matakuliah kurikulum yang belum sync
        List<MatakuliahKurikulum> lokalList;
        if (semester != null) {
            lokalList = matakuliahKurikulumDao.findByStatusAndKurikulumAndSemester(
                    StatusRecord.AKTIF, kurikulum, semester);
        } else {
            lokalList = matakuliahKurikulumDao.findByStatusAndKurikulumAndSemesterNotNull(
                    StatusRecord.AKTIF, kurikulum);
        }

        // Filter yang belum sync DAN SKS > 0
        List<MatakuliahKurikulum> unsyncedList = lokalList.stream()
                .filter(mk -> !mk.isSyncedToFeeder())
                .filter(mk -> mk.getJumlahSks() != null && mk.getJumlahSks() > 0)
                .collect(java.util.stream.Collectors.toList());

        // Hitung yang sudah sync dan SKS 0
        long alreadySyncedCount = lokalList.stream().filter(MatakuliahKurikulum::isSyncedToFeeder).count();
        long sksZeroCount = lokalList.stream()
                .filter(mk -> mk.getJumlahSks() == null || mk.getJumlahSks() == 0)
                .count();

        if (unsyncedList.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tidak ada matakuliah kurikulum yang perlu di-sync",
                    "total", lokalList.size(),
                    "alreadySynced", alreadySyncedCount,
                    "skippedSksZero", sksZeroCount));
        }

        // 3. Process batch
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failedList = new ArrayList<>();
        int matkulInsertedCount = 0;

        for (MatakuliahKurikulum mk : unsyncedList) {
            String kodeLokal = mk.getMatakuliah().getKodeMatakuliah();
            String namaLokal = mk.getMatakuliah().getNamaMatakuliah();

            try {
                // Search matakuliah di Feeder
                Map<String, Object> feederMatkul = searchMatkulInFeeder(kodeLokal, namaLokal, idProdiFeeder);
                String idMatkulFeeder = null;
                boolean matkulInserted = false;

                if (feederMatkul != null && feederMatkul.get("idMatkul") != null) {
                    idMatkulFeeder = (String) feederMatkul.get("idMatkul");
                } else {
                    // Insert matakuliah
                    Map<String, Object> insertMatkulResult = insertMatakuliahToFeeder(mk, idProdiFeeder);
                    if (!(Boolean) insertMatkulResult.get("success")) {
                        failedList.add(Map.of(
                                "id", mk.getId(),
                                "kode", kodeLokal != null ? kodeLokal : "",
                                "nama", namaLokal,
                                "error", "Gagal insert matakuliah: " + insertMatkulResult.get("error")));
                        continue;
                    }
                    idMatkulFeeder = (String) insertMatkulResult.get("idMatkul");
                    matkulInserted = true;
                    matkulInsertedCount++;
                }

                // Insert matkul kurikulum
                Map<String, Object> insertMkResult = insertMatkulKurikulumToFeeder(
                        idKurikulumFeeder, idMatkulFeeder, mk.getSemester(), mk.getAkses());

                if (!(Boolean) insertMkResult.get("success")) {
                    failedList.add(Map.of(
                            "id", mk.getId(),
                            "kode", kodeLokal != null ? kodeLokal : "",
                            "nama", namaLokal,
                            "error", "Gagal insert matkul kurikulum: " + insertMkResult.get("error")));
                    continue;
                }

                boolean alreadyExistsInFeeder = Boolean.TRUE.equals(insertMkResult.get("alreadyExists"));

                // Update lokal
                mk.setStatusFeeder(StatusRecord.AKTIF);
                matakuliahKurikulumDao.save(mk);

                Map<String, Object> successItem = new LinkedHashMap<>();
                successItem.put("id", mk.getId());
                successItem.put("statusFeeder", "AKTIF");
                successItem.put("kode", kodeLokal != null ? kodeLokal : "");
                successItem.put("nama", namaLokal);
                successItem.put("semester", mk.getSemester());
                successItem.put("matkulInserted", matkulInserted);
                successItem.put("alreadyExistsInFeeder", alreadyExistsInFeeder);
                successList.add(successItem);

            } catch (Exception e) {
                log.error("Error processing matkul kurikulum: {} - {}", mk.getId(), e.getMessage());
                failedList.add(Map.of(
                        "id", mk.getId(),
                        "kode", kodeLokal != null ? kodeLokal : "",
                        "nama", namaLokal,
                        "error", e.getMessage()));
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("kurikulum", Map.of(
                "id", kurikulum.getId(),
                "nama", kurikulum.getNamaKurikulum(),
                "idPddikti", idKurikulumFeeder));
        response.put("semester", semester != null ? semester : "ALL");
        response.put("summary", Map.of(
                "total", lokalList.size(),
                "alreadySynced", alreadySyncedCount,
                "skippedSksZero", sksZeroCount,
                "processed", unsyncedList.size(),
                "success", successList.size(),
                "failed", failedList.size(),
                "matkulInserted", matkulInsertedCount));
        response.put("successList", successList);
        if (!failedList.isEmpty()) {
            response.put("failedList", failedList);
        }

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // Private Helper Methods
    // =====================================================

    /**
     * Helper: Search matakuliah di Feeder
     * 
     * PENTING: Di Feeder PDDIKTI, matakuliah dengan kode + nama + sks yang sama
     * dianggap SAMA meskipun beda prodi. Jadi logikanya:
     * 
     * 1. Cari dengan filter: kode + nama + prodi (exact match prodi ini)
     * 2. Jika tidak ditemukan → cari TANPA prodi (mungkin sudah ada dari prodi
     * lain)
     * 3. Jika ditemukan dari prodi lain → pakai id_matkul yang existing
     * 4. Jika benar-benar tidak ada → return null (akan di-create baru)
     */
    private Map<String, Object> searchMatkulInFeeder(String kode, String nama, String idProdi) {
        if (kode == null || kode.isEmpty() || nama == null || nama.isEmpty()) {
            return null;
        }

        // Step 1: Cari dengan prodi spesifik dulu
        String filterWithProdi = FeederFilterBuilder.builder()
                .eq("kode_mata_kuliah", kode)
                .like("nama_mata_kuliah", nama)
                .eq("id_prodi", idProdi)
                .build();
        log.info("Step 1 - Searching matakuliah with prodi filter: {}", filterWithProdi);
        Map<String, Object> result = executeSearch(filterWithProdi, "KODE_NAMA_PRODI");
        if (result != null) {
            return result;
        }

        // Step 2: Tidak ditemukan di prodi ini, cari TANPA prodi
        // (mungkin sudah ada dari prodi lain dengan kode+nama yang sama)
        String filterWithoutProdi = FeederFilterBuilder.builder()
                .eq("kode_mata_kuliah", kode)
                .like("nama_mata_kuliah", nama)
                .build();
        log.info("Step 2 - Searching matakuliah WITHOUT prodi filter: {}", filterWithoutProdi);
        result = executeSearch(filterWithoutProdi, "KODE_NAMA_TANPA_PRODI");
        if (result != null) {
            log.info("Matakuliah ditemukan dari prodi lain - kode: {}, nama: {}, id_matkul: {}",
                    kode, nama, result.get("idMatkul"));
            return result;
        }

        // Tidak ditemukan sama sekali → return null (akan di-create baru)
        log.info("Matakuliah tidak ditemukan di Feeder - kode: {}, nama: {}, prodi: {}", kode, nama, idProdi);
        return null;
    }

    private Map<String, Object> executeSearch(String filter, String matchType) {
        FeederResponse<Object> response = feederService.execute("GetListMataKuliah", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            if (!dataList.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> first = (Map<String, Object>) dataList.get(0);

                return Map.of(
                        "idMatkul", first.get("id_matkul"),
                        "matchType", matchType,
                        "kode", first.get("kode_mata_kuliah") != null ? first.get("kode_mata_kuliah") : "",
                        "nama", first.get("nama_mata_kuliah") != null ? first.get("nama_mata_kuliah") : "",
                        "sks", first.get("sks_mata_kuliah") != null ? first.get("sks_mata_kuliah") : "");
            }
        }
        return null;
    }

    /**
     * Helper: Insert Matakuliah ke Feeder
     */
    private Map<String, Object> insertMatakuliahToFeeder(MatakuliahKurikulum mk, String idProdiFeeder) {
        String kode = mk.getMatakuliah().getKodeMatakuliah();
        String nama = mk.getMatakuliah().getNamaMatakuliah();
        Integer sksTatapMuka = mk.getJumlahSks();

        // Mapping akses ke id_jenis_mata_kuliah
        String idJenisMatkul = null;
        if (mk.getAkses() != null) {
            if (mk.getAkses() == Akses.WAJIB_UMUM) {
                idJenisMatkul = "A"; // Wajib
            } else if (mk.getAkses() == Akses.PILIHAN) {
                idJenisMatkul = "B"; // Pilihan
            }
        }

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("kode_mata_kuliah", kode != null ? kode : "");
        record.put("nama_mata_kuliah", nama);
        record.put("id_prodi", idProdiFeeder);
        if (idJenisMatkul != null) {
            record.put("id_jenis_mata_kuliah", idJenisMatkul);
        }

        record.put("sks_tatap_muka", sksTatapMuka != null ? sksTatapMuka : 0);
        record.put("sks_praktek", 0);
        record.put("sks_praktek_lapangan", 0);
        record.put("sks_simulasi", 0);

        log.info("InsertMataKuliah - kode: {}, nama: {}, id_prodi: {}, jenis: {}, sks_tatap_muka: {}",
                kode, nama, idProdiFeeder, idJenisMatkul, sksTatapMuka);

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
     * Helper: Insert MatkulKurikulum ke Feeder
     */
    private Map<String, Object> insertMatkulKurikulumToFeeder(String idKurikulum, String idMatkul, Integer semester,
            Akses akses) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id_kurikulum", idKurikulum);
        record.put("id_matkul", idMatkul);
        record.put("semester", semester != null ? String.valueOf(semester) : "1");

        String apakahWajib = "0";
        if (akses != null) {
            apakahWajib = String.valueOf(akses.getApakahWajib());
        }
        record.put("apakah_wajib", apakahWajib);

        log.info("InsertMatkulKurikulum - id_kurikulum: {}, id_matkul: {}, semester: {}, akses: {}, apakah_wajib: {}",
                idKurikulum, idMatkul, semester, akses, apakahWajib);

        FeederResponse<Object> response = feederService.insert("InsertMatkulKurikulum", record);

        if (response.isSuccess()) {
            String idMatkulKurikulum = null;
            Object data = response.getData();

            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) data;
                String idKur = dataMap.get("id_kurikulum") != null ? dataMap.get("id_kurikulum").toString() : null;
                String idMat = dataMap.get("id_matkul") != null ? dataMap.get("id_matkul").toString() : null;

                if (idKur != null && idMat != null) {
                    idMatkulKurikulum = idKur + "_" + idMat;
                }
            }

            return Map.of(
                    "success", true,
                    "alreadyExists", false,
                    "idMatkulKurikulum", idMatkulKurikulum != null ? idMatkulKurikulum : "");
        } else {
            String errorDesc = response.getErrorDesc() != null ? response.getErrorDesc().toLowerCase() : "";
            boolean alreadyExists = errorDesc.contains("sudah ada") ||
                    errorDesc.contains("already exist") ||
                    errorDesc.contains("duplicate") ||
                    errorDesc.contains("telah ada") ||
                    errorDesc.contains("sudah terdaftar");

            if (alreadyExists) {
                log.info("Data MatkulKurikulum sudah ada di Feeder (id_kurikulum: {}, id_matkul: {})", idKurikulum,
                        idMatkul);
                return Map.of(
                        "success", true,
                        "alreadyExists", true,
                        "idMatkulKurikulum", idKurikulum + "_" + idMatkul);
            }

            return Map.of(
                    "success", false,
                    "alreadyExists", false,
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
