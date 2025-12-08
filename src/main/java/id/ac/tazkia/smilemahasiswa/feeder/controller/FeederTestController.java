package id.ac.tazkia.smilemahasiswa.feeder.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederKurikulumDto;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import id.ac.tazkia.smilemahasiswa.feeder.dto.SyncResult;
import id.ac.tazkia.smilemahasiswa.feeder.service.FeederAuthService;
import id.ac.tazkia.smilemahasiswa.feeder.service.FeederService;
import id.ac.tazkia.smilemahasiswa.feeder.service.KurikulumFeederService;
import id.ac.tazkia.smilemahasiswa.feeder.service.ProdiSyncService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller untuk testing dan monitoring Feeder API integration.
 * Gunakan endpoint ini untuk memverifikasi koneksi ke PDDIKTI Feeder.
 * 
 * NOTE: Untuk production, tambahkan @PreAuthorize("hasRole('ADMIN')")
 */
@Slf4j
@RestController
@RequestMapping("/api/feeder")
public class FeederTestController {

    private final FeederService feederService;
    private final FeederAuthService authService;
    private final ProdiSyncService prodiSyncService;
    private final KurikulumFeederService kurikulumFeederService;
    private final ProdiDao prodiDao;
    private final KurikulumDao kurikulumDao;
    private final MatakuliahKurikulumDao matakuliahKurikulumDao;

    public FeederTestController(FeederService feederService, FeederAuthService authService,
            ProdiSyncService prodiSyncService,
            KurikulumFeederService kurikulumFeederService,
            ProdiDao prodiDao,
            KurikulumDao kurikulumDao,
            MatakuliahKurikulumDao matakuliahKurikulumDao) {
        this.feederService = feederService;
        this.authService = authService;
        this.prodiSyncService = prodiSyncService;
        this.kurikulumFeederService = kurikulumFeederService;
        this.prodiDao = prodiDao;
        this.kurikulumDao = kurikulumDao;
        this.matakuliahKurikulumDao = matakuliahKurikulumDao;
    }

    /**
     * Test koneksi dengan get token
     * GET /api/feeder/test/token
     */
    @GetMapping("/test/token")
    public Map<String, Object> testToken() {
        log.info("Testing Feeder token acquisition...");

        String token = authService.getToken();

        if (token != null) {
            var tokenData = authService.getTokenData();
            return Map.of(
                    "success", true,
                    "message", "Token acquired successfully",
                    "tokenPreview", token.substring(0, Math.min(20, token.length())) + "...",
                    "obtainedAt", tokenData.getObtainedAt().toString(),
                    "expiresAt", tokenData.getExpiresAt().toString());
        } else {
            return Map.of(
                    "success", false,
                    "message", "Failed to acquire token");
        }
    }

    /**
     * Health check
     * GET /api/feeder/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        boolean healthy = feederService.isHealthy();
        return Map.of(
                "status", healthy ? "UP" : "DOWN",
                "tokenStatus", feederService.getTokenStatus());
    }

    /**
     * Test get data prodi
     * GET /api/feeder/test/prodi
     */
    @GetMapping("/test/prodi")
    public FeederResponse<Object> testGetProdi() {
        log.info("Testing GetProdi...");
        return feederService.execute("GetProdi");
    }

    @GetMapping("/test/listmatakul")
    public FeederResponse<Object> testGetListMatakul(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("Testing GetListMatakul with limit {}...", limit);
        return feederService.execute("GetListMataKuliah", null, null, limit, 0);
    }

    /**
     * Test get data mahasiswa (dengan limit)
     * GET /api/feeder/test/mahasiswa?limit=10
     */
    @GetMapping("/test/mahasiswa")
    public FeederResponse<Object> testGetMahasiswa(
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("Testing GetMahasiswa with limit {}...", limit);
        return feederService.execute("GetMahasiswa", null, null, limit, 0);
    }

    /**
     * Test get data dosen (dengan limit)
     * GET /api/feeder/test/dosen?limit=10
     */
    @GetMapping("/test/dosen")
    public FeederResponse<Object> testGetDosen(
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("Testing GetDosen with limit {}...", limit);
        return feederService.execute("GetDosen", null, null, limit, 0);
    }

    /**
     * Force refresh token
     * POST /api/feeder/token/refresh
     */
    @PostMapping("/token/refresh")
    public Map<String, Object> refreshToken() {
        log.info("Force refreshing Feeder token...");

        String token = authService.forceRefreshToken();

        if (token != null) {
            var tokenData = authService.getTokenData();
            return Map.of(
                    "success", true,
                    "message", "Token refreshed successfully",
                    "expiresAt", tokenData.getExpiresAt().toString());
        } else {
            return Map.of(
                    "success", false,
                    "message", "Failed to refresh token");
        }
    }

    /**
     * Clear cached token
     * DELETE /api/feeder/token
     */
    @DeleteMapping("/token")
    public Map<String, Object> clearToken() {
        log.info("Clearing Feeder token cache...");
        authService.clearToken();
        return Map.of(
                "success", true,
                "message", "Token cache cleared");
    }

    // =====================================================
    // Contoh Endpoint dengan Filter Spesifik
    // =====================================================

    /**
     * Get mahasiswa by NIM
     * GET /api/feeder/mahasiswa/{nim}
     */
    @GetMapping("/mahasiswa/{nim}")
    public FeederResponse<Object> getMahasiswaByNim(@PathVariable String nim) {
        log.info("Getting mahasiswa with NIM: {}", nim);
        String filter = String.format("nim='%s'", nim);
        return feederService.execute("GetMahasiswa", filter);
    }

    /**
     * Get dosen by NIDN
     * GET /api/feeder/dosen/{nidn}
     */
    @GetMapping("/dosen/{nidn}")
    public FeederResponse<Object> getDosenByNidn(@PathVariable String nidn) {
        log.info("Getting dosen with NIDN: {}", nidn);
        String filter = String.format("nidn='%s'", nidn);
        return feederService.execute("GetDosen", filter);
    }

    /**
     * Get mahasiswa by prodi dengan pagination
     * GET /api/feeder/mahasiswa?idProdi=123&limit=100&offset=0
     */
    @GetMapping("/mahasiswa")
    public FeederResponse<Object> getMahasiswaByProdi(
            @RequestParam(required = false) String idProdi,
            @RequestParam(required = false) String nama,
            @RequestParam(defaultValue = "100") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        StringBuilder filter = new StringBuilder();

        if (idProdi != null && !idProdi.isEmpty()) {
            filter.append(String.format("id_prodi='%s'", idProdi));
        }

        if (nama != null && !nama.isEmpty()) {
            if (filter.length() > 0)
                filter.append(" and ");
            filter.append(String.format("nama_mahasiswa like '%%%s%%'", nama));
        }

        String filterStr = filter.length() > 0 ? filter.toString() : null;
        log.info("Getting mahasiswa with filter: {}, limit: {}, offset: {}", filterStr, limit, offset);

        return feederService.execute("GetMahasiswa", filterStr, "nama_mahasiswa ASC", limit, offset);
    }

    /**
     * Get semester aktif
     * GET /api/feeder/semester/aktif
     */
    @GetMapping("/semester/aktif")
    public FeederResponse<Object> getSemesterAktif() {
        log.info("Getting active semester...");
        return feederService.execute("GetSemester", "a_periode_aktif='1'");
    }

    /**
     * Get matakuliah by prodi
     * GET /api/feeder/matakuliah?idProdi=123
     */
    @GetMapping("/matakuliah")
    public FeederResponse<Object> getMatakuliahByProdi(
            @RequestParam String idProdi,
            @RequestParam(defaultValue = "100") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        log.info("Getting matakuliah for prodi: {}", idProdi);
        String filter = String.format("id_prodi='%s'", idProdi);
        return feederService.execute("GetMataKuliah", filter, null, limit, offset);
    }

    // =====================================================
    // Sync Check Endpoints
    // =====================================================

    /**
     * Cek status sinkronisasi Prodi antara database lokal dan Feeder
     * GET /api/feeder/sync/prodi
     * 
     * Response berisi:
     * - matched: Prodi yang sama di kedua sistem
     * - mismatched: Prodi yang berbeda (nama/status beda)
     * - missingInFeeder: Ada di lokal tapi tidak ada di Feeder
     * - missingInLocal: Ada di Feeder tapi tidak ada di lokal
     */
    @GetMapping("/sync/prodi")
    public SyncResult<Map<String, Object>> checkProdiSync() {
        log.info("Checking Prodi synchronization status...");
        return prodiSyncService.checkSync();
    }

    /**
     * Cek summary sinkronisasi Prodi (ringkas, tanpa detail)
     * GET /api/feeder/sync/prodi/summary
     */
    @GetMapping("/sync/prodi/summary")
    public Map<String, Object> checkProdiSyncSummary() {
        log.info("Getting Prodi sync summary...");
        SyncResult<Map<String, Object>> result = prodiSyncService.checkSync();

        return Map.of(
                "synced", result.isSynced(),
                "summary", result.getSummary(),
                "totalLocal", result.getTotalLocal(),
                "totalFeeder", result.getTotalFeeder(),
                "matched", result.getMatchedCount(),
                "mismatched", result.getMismatchedCount(),
                "missingInFeeder", result.getMissingInFeederCount(),
                "missingInLocal", result.getMissingInLocalCount());
    }

    // =====================================================
    // Kurikulum Feeder Endpoints
    // =====================================================

    /**
     * Get kurikulum dari Feeder berdasarkan Prodi ID lokal
     * GET /api/feeder/kurikulum/prodi/{prodiId}
     * 
     * @param prodiId ID Prodi dari database lokal
     * @return List kurikulum dari Feeder untuk prodi tersebut
     */
    @GetMapping("/kurikulum/prodi/{prodiId}")
    public ResponseEntity<?> getKurikulumByProdi(@PathVariable String prodiId) {
        log.info("Getting kurikulum from Feeder for prodi: {}", prodiId);

        Prodi prodi = prodiDao.findById(prodiId).orElse(null);
        if (prodi == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak ditemukan dengan ID: " + prodiId));
        }

        if (prodi.getKodePddikti() == null || prodi.getKodePddikti().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak memiliki kodePddikti: " + prodi.getNamaProdi()));
        }

        try {
            List<FeederKurikulumDto> kurikulumList = kurikulumFeederService.getKurikulumByProdi(prodi);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "prodi", Map.of(
                            "id", prodi.getId(),
                            "nama", prodi.getNamaProdi(),
                            "kodePddikti", prodi.getKodePddikti()),
                    "totalKurikulum", kurikulumList.size(),
                    "data", kurikulumList));
        } catch (Exception e) {
            log.error("Error getting kurikulum from Feeder", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Cek sinkronisasi Kurikulum untuk Prodi tertentu
     * GET /api/feeder/sync/kurikulum/prodi/{prodiId}
     */
    @GetMapping("/sync/kurikulum/prodi/{prodiId}")
    public ResponseEntity<?> checkKurikulumSync(@PathVariable String prodiId) {
        log.info("Checking kurikulum sync for prodi: {}", prodiId);

        Prodi prodi = prodiDao.findById(prodiId).orElse(null);
        if (prodi == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak ditemukan dengan ID: " + prodiId));
        }

        if (prodi.getKodePddikti() == null || prodi.getKodePddikti().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak memiliki kodePddikti"));
        }

        try {
            SyncResult<Map<String, Object>> result = kurikulumFeederService.checkSync(prodi);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error checking kurikulum sync", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Sync idPddikti untuk semua kurikulum di prodi tertentu
     * POST /api/feeder/sync/kurikulum/prodi/{prodiId}/id-pddikti
     * 
     * Akan mencari kurikulum di Feeder berdasarkan nama, lalu update idPddikti di
     * database lokal
     */
    @PostMapping("/sync/kurikulum/prodi/{prodiId}/id-pddikti")
    public ResponseEntity<?> syncKurikulumIdPddikti(@PathVariable String prodiId) {
        log.info("Syncing idPddikti for kurikulum in prodi: {}", prodiId);

        Prodi prodi = prodiDao.findById(prodiId).orElse(null);
        if (prodi == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak ditemukan dengan ID: " + prodiId));
        }

        if (prodi.getKodePddikti() == null || prodi.getKodePddikti().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak memiliki kodePddikti"));
        }

        try {
            Map<String, Object> result = kurikulumFeederService.syncIdPddikti(prodi);
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error syncing kurikulum idPddikti", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Get semua Prodi AKTIF dengan kodePddikti
     * GET /api/feeder/prodi/aktif
     * 
     * Berguna untuk melihat prodi mana saja yang bisa di-sync
     */
    @GetMapping("/prodi/aktif")
    public ResponseEntity<?> getProdiAktif() {
        List<Prodi> prodiList = prodiDao.findByStatus(StatusRecord.AKTIF);

        List<Map<String, Object>> result = prodiList.stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "namaProdi", p.getNamaProdi(),
                        "kodePddikti", p.getKodePddikti() != null ? p.getKodePddikti() : "",
                        "hasKodePddikti", p.getKodePddikti() != null && !p.getKodePddikti().isEmpty()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "total", result.size(),
                "data", result));
    }

    /**
     * Get daftar kurikulum untuk prodi tertentu (lokal)
     * GET /api/feeder/kurikulum-lokal/prodi/{prodiId}
     * 
     * Berguna untuk mendapatkan ID kurikulum
     */
    @GetMapping("/kurikulum-lokal/prodi/{prodiId}")
    public ResponseEntity<?> getKurikulumLokalByProdi(@PathVariable String prodiId) {
        log.info("Getting local kurikulum for prodi: {}", prodiId);

        Prodi prodi = prodiDao.findById(prodiId).orElse(null);
        if (prodi == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak ditemukan dengan ID: " + prodiId));
        }

        List<Kurikulum> kurikulumList = kurikulumDao.findByProdiAndStatusNotIn(
                prodi,
                Arrays.asList(StatusRecord.HAPUS));

        List<Map<String, Object>> result = kurikulumList.stream()
                .map(k -> Map.<String, Object>of(
                        "id", k.getId(),
                        "namaKurikulum", k.getNamaKurikulum(),
                        "tahunKurikulum", k.getTahunKurikulum() != null ? k.getTahunKurikulum() : "",
                        "status", k.getStatus().toString()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "prodi", Map.of(
                        "id", prodi.getId(),
                        "nama", prodi.getNamaProdi()),
                "total", result.size(),
                "data", result));
    }

    /**
     * Debug: Cek apakah kodePddikti prodi lokal cocok dengan id_prodi di Feeder
     * GET /api/feeder/debug/prodi/{prodiId}
     */
    @GetMapping("/debug/prodi/{prodiId}")
    public ResponseEntity<?> debugProdi(@PathVariable String prodiId) {
        Prodi prodi = prodiDao.findById(prodiId).orElse(null);
        if (prodi == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Prodi tidak ditemukan"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("lokalProdi", Map.of(
                "id", prodi.getId(),
                "namaProdi", prodi.getNamaProdi(),
                "kodePddikti", prodi.getKodePddikti() != null ? prodi.getKodePddikti() : "NULL"));

        // Cek di Feeder
        if (prodi.getKodePddikti() != null && !prodi.getKodePddikti().isEmpty()) {
            String filter = String.format("id_prodi='%s'", prodi.getKodePddikti());
            FeederResponse<Object> response = feederService.execute("GetProdi", filter);

            if (response.isSuccess() && response.getData() != null) {
                List<?> dataList = (List<?>) response.getData();
                result.put("feederProdiByKodePddikti", dataList.isEmpty() ? "NOT FOUND" : dataList.get(0));
            } else {
                result.put("feederProdiByKodePddikti", "NOT FOUND - " + response.getErrorDesc());
            }

            // Coba cari by nama
            String filterNama = String.format("nama_program_studi LIKE '%%%s%%'", prodi.getNamaProdi());
            FeederResponse<Object> responseNama = feederService.execute("GetProdi", filterNama);

            if (responseNama.isSuccess() && responseNama.getData() != null) {
                result.put("feederProdiByNama", responseNama.getData());
            }
        }

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // Search Matakuliah di Feeder
    // =====================================================

    /**
     * Search matakuliah di Feeder (GetListMataKuliah)
     * GET /api/feeder/search/matakuliah?kode=MIDU.006&nama=Dirasah&idProdi=xxx
     * 
     * Logika:
     * 1. Cari dengan filter prodi dulu (jika idProdi diisi)
     * 2. Jika tidak ada, cari tanpa prodi dan ambil 1
     * 
     * @param kode    Kode matakuliah (exact match)
     * @param nama    Nama matakuliah (LIKE search)
     * @param idProdi ID Prodi di Feeder (optional, untuk filter by prodi dulu)
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
            StringBuilder baseFilter = new StringBuilder();

            if (kode != null && !kode.isEmpty()) {
                baseFilter.append(String.format("kode_mata_kuliah='%s'", kode));
            }

            if (nama != null && !nama.isEmpty()) {
                if (baseFilter.length() > 0) {
                    baseFilter.append(" And ");
                }
                String escapedNama = nama.replace("'", "''");
                baseFilter.append(String.format("nama_mata_kuliah like '%%%s%%'", escapedNama));
            }

            String baseFilterStr = baseFilter.toString();
            List<?> dataList = null;
            String usedFilter = "";
            String matchType = "";

            // Step 1: Cari dengan prodi dulu (jika idProdi diisi)
            if (idProdi != null && !idProdi.isEmpty()) {
                String filterWithProdi = baseFilterStr + String.format(" And id_prodi='%s'", idProdi);
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

    // =====================================================
    // Prepare InsertMatkulKurikulum
    // =====================================================

    /**
     * Get matakuliah kurikulum lokal dan cari id_matkul di Feeder
     * GET
     * /api/feeder/prepare-insert/matkul-kurikulum?prodiId=xxx&kurikulumId=yyy&semester=1
     * 
     * Logika pencarian matakuliah di Feeder:
     * 1. Cari dengan filter prodi dulu
     * 2. Jika tidak ditemukan, cari tanpa prodi dan ambil 1
     * 
     * Response format per item:
     * {
     * "foundInFeeder": false,
     * "kodeLokal": "MIDU.006",
     * "namaLokal": "Dirasah Islamiah",
     * "matkulFeeder": "",
     * "semester": 1,
     * "sksLokal": 3,
     * "message": ""
     * }
     * 
     * @param prodiId     ID prodi lokal (required)
     * @param kurikulumId ID kurikulum lokal (required)
     * @param semester    Semester (1-8), optional. Jika tidak diisi ambil semua.
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

            Map<String, Object> item = new LinkedHashMap<>(); // LinkedHashMap untuk preserve order

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
     * Helper: Search matakuliah di Feeder
     * Logika:
     * 1. Cari dengan prodi dulu (kode + prodi, atau nama + prodi)
     * 2. Jika tidak ada, cari tanpa prodi dan ambil 1
     */
    private Map<String, Object> searchMatkulInFeeder(String kode, String nama, String idProdi) {
        // === Step 1: Cari dengan filter prodi ===

        // Prioritas 1: Search by kode + prodi
        if (kode != null && !kode.isEmpty()) {
            String filter = String.format("kode_mata_kuliah='%s' And id_prodi='%s'", kode, idProdi);
            Map<String, Object> result = executeSearch(filter, "KODE_PRODI");
            if (result != null)
                return result;
        }

        // Prioritas 2: Search by nama + prodi
        if (nama != null && !nama.isEmpty()) {
            String escapedNama = nama.replace("'", "''");
            String filter = String.format("nama_mata_kuliah like '%%%s%%' And id_prodi='%s'", escapedNama, idProdi);
            Map<String, Object> result = executeSearch(filter, "NAMA_PRODI");
            if (result != null)
                return result;
        }

        // === Step 2: Cari tanpa prodi (ambil 1) ===

        // Prioritas 3: Search by kode saja
        if (kode != null && !kode.isEmpty()) {
            String filter = String.format("kode_mata_kuliah='%s'", kode);
            Map<String, Object> result = executeSearch(filter, "KODE_TANPA_PRODI");
            if (result != null)
                return result;
        }

        // Prioritas 4: Search by nama saja
        if (nama != null && !nama.isEmpty()) {
            String escapedNama = nama.replace("'", "''");
            String filter = String.format("nama_mata_kuliah like '%%%s%%'", escapedNama);
            Map<String, Object> result = executeSearch(filter, "NAMA_TANPA_PRODI");
            if (result != null)
                return result;
        }

        return null;
    }

    private Map<String, Object> executeSearch(String filter, String matchType) {
        FeederResponse<Object> response = feederService.execute("GetListMataKuliah", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            if (!dataList.isEmpty()) {
                // Ambil yang pertama
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

    // ==================== INSERT MATKUL KURIKULUM ====================

    /**
     * Insert single MatakuliahKurikulum ke Feeder
     * POST /api/feeder/insert/matkul-kurikulum?id=xxx
     * 
     * Flow:
     * 1. Search matakuliah di Feeder
     * 2. Jika tidak ditemukan → InsertMataKuliah dulu
     * 3. InsertMatkulKurikulum dengan id_matkul yang didapat
     * 4. Update idPddikti di MatakuliahKurikulum lokal
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

        String idMatkulKurikulumFeeder = (String) insertMkResult.get("idMatkulKurikulum");

        // 6. Update statusFeeder di lokal
        mk.setStatusFeeder(StatusRecord.AKTIF);
        matakuliahKurikulumDao.save(mk);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Berhasil insert MatkulKurikulum ke Feeder");
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

        // Filter yang belum sync
        List<MatakuliahKurikulum> unsyncedList = lokalList.stream()
                .filter(mk -> !mk.isSyncedToFeeder())
                .collect(java.util.stream.Collectors.toList());

        if (unsyncedList.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Semua matakuliah kurikulum sudah di-sync",
                    "total", lokalList.size(),
                    "alreadySynced", lokalList.size()));
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

                String idMatkulKurikulumFeeder = (String) insertMkResult.get("idMatkulKurikulum");

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
                "alreadySynced", lokalList.size() - unsyncedList.size(),
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

    /**
     * Helper: Insert Matakuliah ke Feeder
     * Mapping akses: WAJIB_UMUM → "A", PILIHAN → "B", else → null
     * 
     * Field SKS:
     * - sks_tatap_muka: diambil dari jumlahSks MatakuliahKurikulum
     * - sks_praktek: 0
     * - sks_praktek_lapangan: 0
     * - sks_simulasi: 0
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
        // id_kelompok_mata_kuliah = null (tidak diisi)

        // Field SKS - sks_tatap_muka dari jumlahSks, sisanya 0
        record.put("sks_tatap_muka", sksTatapMuka != null ? sksTatapMuka : 0);
        record.put("sks_praktek", 0);
        record.put("sks_praktek_lapangan", 0);
        record.put("sks_simulasi", 0);

        log.info("InsertMataKuliah - kode: {}, nama: {}, id_prodi: {}, jenis: {}, sks_tatap_muka: {}",
                kode, nama, idProdiFeeder, idJenisMatkul, sksTatapMuka);

        FeederResponse<Object> response = feederService.insert("InsertMataKuliah", record);

        if (response.isSuccess()) {
            // Parse id_matkul dari response
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
     * 
     * @param akses - akses matakuliah untuk menentukan apakah_wajib
     *              (WAJIB_UMUM/WAJIB_PRODI = 1, PILIHAN = 0)
     */
    private Map<String, Object> insertMatkulKurikulumToFeeder(String idKurikulum, String idMatkul, Integer semester,
            Akses akses) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id_kurikulum", idKurikulum);
        record.put("id_matkul", idMatkul);
        record.put("semester", semester != null ? semester : 1);

        // apakah_wajib diambil dari enum Akses
        int apakahWajib = akses != null ? akses.getApakahWajib() : 0;
        record.put("apakah_wajib", apakahWajib);

        log.info("InsertMatkulKurikulum - id_kurikulum: {}, id_matkul: {}, semester: {}, apakah_wajib: {}",
                idKurikulum, idMatkul, semester, apakahWajib);

        FeederResponse<Object> response = feederService.insert("InsertMatkulKurikulum", record);

        // Log full response untuk debug
        log.info("InsertMatkulKurikulum response - success: {}, data: {}, errorCode: {}, errorDesc: {}",
                response.isSuccess(), response.getData(), response.getErrorCode(), response.getErrorDesc());

        if (response.isSuccess()) {
            // Response dari InsertMatkulKurikulum berisi {id_kurikulum, id_matkul}
            // ID matkul kurikulum di Feeder adalah kombinasi id_kurikulum + id_matkul
            String idMatkulKurikulum = null;
            Object data = response.getData();

            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) data;
                String idKur = dataMap.get("id_kurikulum") != null ? dataMap.get("id_kurikulum").toString() : null;
                String idMat = dataMap.get("id_matkul") != null ? dataMap.get("id_matkul").toString() : null;

                // Gabungkan sebagai identifier unik
                if (idKur != null && idMat != null) {
                    idMatkulKurikulum = idKur + "_" + idMat;
                }
            }

            log.info("InsertMatkulKurikulum extracted idMatkulKurikulum: {}", idMatkulKurikulum);

            return Map.of(
                    "success", true,
                    "idMatkulKurikulum", idMatkulKurikulum != null ? idMatkulKurikulum : "");
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
            log.debug("extractIdFromResponse: data is null");
            return null;
        }

        try {
            // Response bisa dalam berbagai format
            Object data = response.getData();
            log.debug("extractIdFromResponse: data type = {}, data = {}", data.getClass().getName(), data);

            if (data instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) data;
                log.debug("extractIdFromResponse: Map keys = {}", dataMap.keySet());
                Object value = dataMap.get(key);
                return value != null ? value.toString() : null;
            } else if (data instanceof List) {
                List<?> dataList = (List<?>) data;
                if (!dataList.isEmpty() && dataList.get(0) instanceof Map) {
                    Map<String, Object> first = (Map<String, Object>) dataList.get(0);
                    log.debug("extractIdFromResponse: List[0] Map keys = {}", first.keySet());
                    Object value = first.get(key);
                    return value != null ? value.toString() : null;
                }
            } else if (data instanceof String) {
                // Kadang response langsung return ID sebagai string
                return (String) data;
            }
        } catch (Exception e) {
            log.warn("Error extracting {} from response: {}", key, e.getMessage());
        }

        return null;
    }
}
