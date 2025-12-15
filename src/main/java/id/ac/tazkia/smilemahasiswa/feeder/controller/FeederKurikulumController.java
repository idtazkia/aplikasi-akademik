package id.ac.tazkia.smilemahasiswa.feeder.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.tazkia.smilemahasiswa.dao.KurikulumDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.entity.Kurikulum;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import id.ac.tazkia.smilemahasiswa.feeder.dto.SyncResult;
import id.ac.tazkia.smilemahasiswa.feeder.service.FeederService;
import id.ac.tazkia.smilemahasiswa.feeder.service.KurikulumFeederService;
import id.ac.tazkia.smilemahasiswa.feeder.util.FeederFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller untuk sinkronisasi data Kurikulum dengan Feeder PDDIKTI.
 * 
 * Endpoints:
 * - GET /api/feeder/kurikulum/prodi/{prodiId} - Get kurikulum dari Feeder
 * - GET /api/feeder/sync/kurikulum/prodi/{prodiId} - Cek status sinkronisasi
 * - POST /api/feeder/sync/kurikulum/prodi/{prodiId}/id-pddikti - Sync idPddikti
 * - GET /api/feeder/kurikulum-lokal - List kurikulum lokal
 */
@Slf4j
@RestController
@RequestMapping("/api/feeder")
@RequiredArgsConstructor
public class FeederKurikulumController {

    private final FeederService feederService;
    private final KurikulumFeederService kurikulumFeederService;
    private final ProdiDao prodiDao;
    private final KurikulumDao kurikulumDao;

    /**
     * Get kurikulum dari Feeder berdasarkan prodi
     * GET /api/feeder/kurikulum/prodi/{prodiId}
     */
    @GetMapping("/kurikulum/prodi/{prodiId}")
    public ResponseEntity<?> getKurikulumFromFeeder(@PathVariable String prodiId) {
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
                    "message", "Prodi tidak memiliki kodePddikti"));
        }

        try {
            // Pertama, dapatkan id_prodi (UUID) dari Feeder berdasarkan kode_program_studi
            String filterProdi = FeederFilterBuilder.builder()
                    .eq("kode_program_studi", prodi.getKodePddikti())
                    .build();
            FeederResponse<Object> prodiResponse = feederService.execute("GetProdi", filterProdi);

            if (!prodiResponse.isSuccess() || prodiResponse.getData() == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Prodi tidak ditemukan di Feeder dengan kode: " + prodi.getKodePddikti(),
                        "errorDesc", prodiResponse.getErrorDesc()));
            }

            List<?> prodiList = (List<?>) prodiResponse.getData();
            if (prodiList.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Prodi tidak ditemukan di Feeder dengan kode: " + prodi.getKodePddikti()));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> prodiFeeder = (Map<String, Object>) prodiList.get(0);
            String idProdiFeeder = (String) prodiFeeder.get("id_prodi");

            // Kemudian, get kurikulum menggunakan id_prodi UUID
            String filterKurikulum = FeederFilterBuilder.builder()
                    .eq("id_prodi", idProdiFeeder)
                    .build();
            FeederResponse<Object> response = feederService.execute("GetKurikulum", filterKurikulum);

            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "prodi", Map.of(
                                "id", prodi.getId(),
                                "namaProdi", prodi.getNamaProdi(),
                                "kodePddikti", prodi.getKodePddikti(),
                                "idProdiFeeder", idProdiFeeder),
                        "data", response.getData()));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "errorCode", response.getErrorCode(),
                        "errorDesc", response.getErrorDesc()));
            }
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
     * Get semua kurikulum lokal dengan status sync
     * GET /api/feeder/kurikulum-lokal
     */
    @GetMapping("/kurikulum-lokal")
    public ResponseEntity<?> getAllKurikulumLokal() {
        List<Kurikulum> kurikulumList = kurikulumDao.findByStatusNotIn(
                Arrays.asList(StatusRecord.HAPUS));

        List<Map<String, Object>> result = kurikulumList.stream()
                .map(k -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", k.getId());
                    item.put("namaKurikulum", k.getNamaKurikulum());
                    item.put("tahunKurikulum", k.getTahunKurikulum() != null ? k.getTahunKurikulum() : "");
                    item.put("status", k.getStatus().toString());
                    item.put("idPddikti", k.getIdPddikti() != null ? k.getIdPddikti() : "");
                    item.put("hasSyncedToFeeder", k.getIdPddikti() != null && !k.getIdPddikti().isEmpty());
                    if (k.getProdi() != null) {
                        item.put("prodi", Map.of(
                                "id", k.getProdi().getId(),
                                "nama", k.getProdi().getNamaProdi(),
                                "kodePddikti",
                                k.getProdi().getKodePddikti() != null ? k.getProdi().getKodePddikti() : ""));
                    }
                    return item;
                })
                .collect(Collectors.toList());

        long syncedCount = result.stream()
                .filter(k -> Boolean.TRUE.equals(k.get("hasSyncedToFeeder")))
                .count();

        return ResponseEntity.ok(Map.of(
                "total", result.size(),
                "synced", syncedCount,
                "notSynced", result.size() - syncedCount,
                "data", result));
    }

    /**
     * Get kurikulum lokal by ID dengan detail
     * GET /api/feeder/kurikulum-lokal/{kurikulumId}
     */
    @GetMapping("/kurikulum-lokal/{kurikulumId}")
    public ResponseEntity<?> getKurikulumLokalById(@PathVariable String kurikulumId) {
        log.info("Getting kurikulum lokal by ID: {}", kurikulumId);

        Kurikulum kurikulum = kurikulumDao.findById(kurikulumId).orElse(null);
        if (kurikulum == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kurikulum tidak ditemukan dengan ID: " + kurikulumId));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", kurikulum.getId());
        result.put("namaKurikulum", kurikulum.getNamaKurikulum());
        result.put("tahunKurikulum", kurikulum.getTahunKurikulum() != null ? kurikulum.getTahunKurikulum() : "");
        result.put("status", kurikulum.getStatus().toString());
        result.put("idPddikti", kurikulum.getIdPddikti() != null ? kurikulum.getIdPddikti() : "");
        result.put("hasSyncedToFeeder", kurikulum.getIdPddikti() != null && !kurikulum.getIdPddikti().isEmpty());

        if (kurikulum.getProdi() != null) {
            Prodi prodi = kurikulum.getProdi();
            result.put("prodi", Map.of(
                    "id", prodi.getId(),
                    "nama", prodi.getNamaProdi(),
                    "kodePddikti", prodi.getKodePddikti() != null ? prodi.getKodePddikti() : ""));

            // Cek di Feeder jika punya idPddikti
            if (kurikulum.getIdPddikti() != null && !kurikulum.getIdPddikti().isEmpty()) {
                String filter = FeederFilterBuilder.builder()
                        .eq("id_kurikulum", kurikulum.getIdPddikti())
                        .build();
                FeederResponse<Object> response = feederService.execute("GetKurikulum", filter);

                if (response.isSuccess() && response.getData() != null) {
                    List<?> dataList = (List<?>) response.getData();
                    if (!dataList.isEmpty()) {
                        result.put("feederData", dataList.get(0));
                    } else {
                        result.put("feederData", "NOT FOUND in Feeder");
                    }
                }
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result));
    }

    /**
     * Debug: Get kurikulum dari Feeder by idPddikti
     * GET /api/feeder/debug/kurikulum/{idPddikti}
     */
    @GetMapping("/debug/kurikulum/{idPddikti}")
    public ResponseEntity<?> debugKurikulum(@PathVariable String idPddikti) {
        log.info("Debug kurikulum by idPddikti: {}", idPddikti);

        String filter = FeederFilterBuilder.builder()
                .eq("id_kurikulum", idPddikti)
                .build();
        FeederResponse<Object> response = feederService.execute("GetKurikulum", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            if (!dataList.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "found", true,
                        "data", dataList.get(0)));
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "found", false,
                "message", "Kurikulum tidak ditemukan di Feeder dengan id_kurikulum: " + idPddikti));
    }
}
