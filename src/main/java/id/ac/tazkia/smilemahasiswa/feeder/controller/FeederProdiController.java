package id.ac.tazkia.smilemahasiswa.feeder.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.tazkia.smilemahasiswa.dao.KurikulumDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.entity.Kurikulum;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import id.ac.tazkia.smilemahasiswa.feeder.dto.SyncResult;
import id.ac.tazkia.smilemahasiswa.feeder.service.FeederService;
import id.ac.tazkia.smilemahasiswa.feeder.service.ProdiSyncService;
import id.ac.tazkia.smilemahasiswa.feeder.util.FeederFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller untuk sinkronisasi data Prodi dengan Feeder PDDIKTI.
 * 
 * Endpoints:
 * - GET /api/feeder/sync/prodi - Cek status sinkronisasi
 * - GET /api/feeder/sync/prodi/summary - Summary sinkronisasi
 * - GET /api/feeder/prodi/aktif - Get prodi aktif dengan kodePddikti
 * - GET /api/feeder/debug/prodi/{prodiId} - Debug prodi mapping
 */
@Slf4j
@RestController
@RequestMapping("/api/feeder")
@RequiredArgsConstructor
public class FeederProdiController {

    private final FeederService feederService;
    private final ProdiSyncService prodiSyncService;
    private final ProdiDao prodiDao;
    private final KurikulumDao kurikulumDao;

    /**
     * Cek status sinkronisasi Prodi antara database lokal dan Feeder
     * GET /api/feeder/sync/prodi
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

    /**
     * Get semua Prodi AKTIF dengan kodePddikti
     * GET /api/feeder/prodi/aktif
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
            String filter = FeederFilterBuilder.builder()
                    .eq("id_prodi", prodi.getKodePddikti())
                    .build();
            FeederResponse<Object> response = feederService.execute("GetProdi", filter);

            if (response.isSuccess() && response.getData() != null) {
                List<?> dataList = (List<?>) response.getData();
                result.put("feederProdiByKodePddikti", dataList.isEmpty() ? "NOT FOUND" : dataList.get(0));
            } else {
                result.put("feederProdiByKodePddikti", "NOT FOUND - " + response.getErrorDesc());
            }

            // Coba cari by nama
            String filterNama = FeederFilterBuilder.builder()
                    .like("nama_program_studi", prodi.getNamaProdi())
                    .build();
            FeederResponse<Object> responseNama = feederService.execute("GetProdi", filterNama);

            if (responseNama.isSuccess() && responseNama.getData() != null) {
                result.put("feederProdiByNama", responseNama.getData());
            }
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Get mahasiswa by NIM
     * GET /api/feeder/mahasiswa/{nim}
     */
    @GetMapping("/mahasiswa/{nim}")
    public FeederResponse<Object> getMahasiswaByNim(@PathVariable String nim) {
        log.info("Getting mahasiswa with NIM: {}", nim);
        String filter = FeederFilterBuilder.builder()
                .eq("nim", nim)
                .build();
        return feederService.execute("GetMahasiswa", filter);
    }

    /**
     * Get dosen by NIDN
     * GET /api/feeder/dosen/{nidn}
     */
    @GetMapping("/dosen/{nidn}")
    public FeederResponse<Object> getDosenByNidn(@PathVariable String nidn) {
        log.info("Getting dosen with NIDN: {}", nidn);
        String filter = FeederFilterBuilder.builder()
                .eq("nidn", nidn)
                .build();
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

        FeederFilterBuilder filterBuilder = FeederFilterBuilder.builder();

        if (idProdi != null && !idProdi.isEmpty()) {
            filterBuilder.eq("id_prodi", idProdi);
        }

        if (nama != null && !nama.isEmpty()) {
            filterBuilder.like("nama_mahasiswa", nama);
        }

        String filter = filterBuilder.build();
        log.info("Getting mahasiswa with filter: {}, limit: {}, offset: {}", filter, limit, offset);

        return feederService.execute("GetMahasiswa", filter, "nama_mahasiswa ASC", limit, offset);
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
        String filter = FeederFilterBuilder.builder()
                .eq("id_prodi", idProdi)
                .build();
        return feederService.execute("GetMataKuliah", filter, null, limit, offset);
    }
}
