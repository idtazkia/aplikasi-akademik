package id.ac.tazkia.smilemahasiswa.feeder.controller;

import java.util.ArrayList;
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
 * Controller untuk sinkronisasi Nilai Perkuliahan dengan Feeder PDDIKTI.
 * 
 * Prerequisites:
 * - Peserta kelas harus sudah ter-sync ke Feeder (statusFeeder = AKTIF)
 * - Nilai harus sudah final (finalisasi = "FINAL")
 * - Grade harus sudah ada
 * 
 * Endpoints:
 * - GET /api/feeder/prepare-update/nilai-perkuliahan - Prepare daftar nilai
 * untuk sync
 * - POST /api/feeder/update/nilai-perkuliahan/batch - Batch update nilai
 * perkuliahan (dengan limit)
 * - POST /api/feeder/update/nilai-perkuliahan/jadwal - Update nilai per jadwal
 * (recommended untuk data besar)
 * 
 * PERFORMANCE NOTES:
 * - Untuk data besar (>100 mahasiswa), gunakan endpoint per jadwal
 * - Batch endpoint memiliki limit default 200 mahasiswa per request
 * - Estimasi waktu: ~500ms per mahasiswa
 */
@Slf4j
@RestController
@RequestMapping("/api/feeder")
@RequiredArgsConstructor
public class FeederNilaiController {

    private static final int DEFAULT_BATCH_LIMIT = 200; // Max mahasiswa per batch request
    private static final int ESTIMATED_MS_PER_CALL = 500; // Estimasi waktu per API call

    private final FeederService feederService;
    private final ProdiDao prodiDao;
    private final TahunAkademikDao tahunAkademikDao;
    private final JadwalDao jadwalDao;
    private final KrsDetailDao krsDetailDao;

    /**
     * Prepare update nilai perkuliahan ke Feeder
     * GET /api/feeder/prepare-update/nilai-perkuliahan?tahunAkademik=xxx&prodi=yyy
     * 
     * Menampilkan ringkasan nilai per jadwal yang siap untuk sync
     */
    @GetMapping("/prepare-update/nilai-perkuliahan")
    public ResponseEntity<?> prepareUpdateNilaiPerkuliahan(
            @RequestParam String tahunAkademik,
            @RequestParam String prodi) {

        log.info("Prepare update nilai perkuliahan - tahunAkademik: {}, prodi: {}", tahunAkademik, prodi);

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

            // Collect data
            List<Map<String, Object>> jadwalList = new ArrayList<>();
            int totalPeserta = 0;
            int readyToSync = 0;
            int alreadySynced = 0;
            int notFinal = 0;
            int notSyncedPeserta = 0;

            for (Jadwal jadwal : syncedJadwals) {
                Map<String, Object> info = new HashMap<>();
                info.put("jadwalId", jadwal.getId());
                info.put("matakuliah", jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                info.put("kelas", jadwal.getKelas().getNamaKelas());
                info.put("idKelasKuliahFeeder", jadwal.getIdKelasKuliahFeeder());

                // Get peserta untuk jadwal ini
                List<KrsDetail> pesertaList = krsDetailDao.findByJadwalAndStatus(jadwal, StatusRecord.AKTIF);
                int jadwalTotal = pesertaList.size();
                int jadwalReady = 0;
                int jadwalSynced = 0;
                int jadwalNotFinal = 0;
                int jadwalNotSyncedPeserta = 0;

                for (KrsDetail krs : pesertaList) {
                    if (krs.getMahasiswa() == null) {
                        continue;
                    }

                    // Check status
                    if (krs.isNilaiSyncedToFeeder()) {
                        jadwalSynced++;
                        continue;
                    }

                    // Check apakah peserta sudah di-sync ke Feeder
                    if (!krs.isSyncedToFeeder()) {
                        jadwalNotSyncedPeserta++;
                        continue;
                    }

                    // Check apakah nilai sudah final
                    if (!"FINAL".equals(krs.getFinalisasi())) {
                        jadwalNotFinal++;
                        continue;
                    }

                    // Check apakah grade sudah ada
                    if (krs.getGrade() == null || krs.getGrade().trim().isEmpty()) {
                        jadwalNotFinal++;
                        continue;
                    }

                    jadwalReady++;
                }

                totalPeserta += jadwalTotal;
                readyToSync += jadwalReady;
                alreadySynced += jadwalSynced;
                notFinal += jadwalNotFinal;
                notSyncedPeserta += jadwalNotSyncedPeserta;

                info.put("totalPeserta", jadwalTotal);
                info.put("readyToSync", jadwalReady);
                info.put("alreadySynced", jadwalSynced);
                info.put("notFinal", jadwalNotFinal);
                info.put("notSyncedPeserta", jadwalNotSyncedPeserta);
                info.put("canSync", jadwalReady > 0);

                jadwalList.add(info);
            }

            // Hitung estimasi waktu dan rekomendasi
            int estimatedSeconds = (readyToSync * ESTIMATED_MS_PER_CALL) / 1000;
            int estimatedMinutes = estimatedSeconds / 60;
            String estimatedTime = estimatedMinutes > 0
                    ? String.format("%d menit %d detik", estimatedMinutes, estimatedSeconds % 60)
                    : String.format("%d detik", estimatedSeconds);

            // Rekomendasi berdasarkan jumlah data
            String recommendation;
            if (readyToSync == 0) {
                recommendation = "Tidak ada nilai yang perlu di-sync";
            } else if (readyToSync <= DEFAULT_BATCH_LIMIT) {
                recommendation = "Gunakan batch endpoint: POST /api/feeder/update/nilai-perkuliahan/batch";
            } else {
                int batches = (int) Math.ceil((double) readyToSync / DEFAULT_BATCH_LIMIT);
                recommendation = String.format(
                        "Data besar (%d mahasiswa). Gunakan endpoint per jadwal atau batch dengan limit. " +
                                "Jika pakai batch, perlu %d kali request dengan limit=%d",
                        readyToSync, batches, DEFAULT_BATCH_LIMIT);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tahunAkademik", Map.of(
                    "id", ta.getId(),
                    "nama", ta.getNamaTahunAkademik()));
            response.put("prodi", Map.of(
                    "id", p.getId(),
                    "nama", p.getNamaProdi()));
            response.put("totalJadwal", syncedJadwals.size());
            response.put("totalPeserta", totalPeserta);
            response.put("readyToSync", readyToSync);
            response.put("alreadySynced", alreadySynced);
            response.put("notFinal", notFinal);
            response.put("notSyncedPeserta", notSyncedPeserta);
            response.put("estimatedTime", estimatedTime);
            response.put("recommendation", recommendation);
            response.put("batchLimit", DEFAULT_BATCH_LIMIT);
            response.put("data", jadwalList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error preparing update nilai perkuliahan", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Batch update nilai perkuliahan ke Feeder
     * POST
     * /api/feeder/update/nilai-perkuliahan/batch?tahunAkademik=xxx&prodi=yyy&limit=200&offset=0
     * 
     * OPTIMIZED: Batch fetch semua data di awal
     * 
     * @param tahunAkademik ID tahun akademik
     * @param prodi         ID prodi
     * @param limit         Maksimal data yang diproses per request (default: 200)
     * @param offset        Skip data sejumlah offset (untuk pagination, default: 0)
     */
    @PostMapping("/update/nilai-perkuliahan/batch")
    public ResponseEntity<SyncResult<String>> batchUpdateNilaiPerkuliahan(
            @RequestParam String tahunAkademik,
            @RequestParam String prodi,
            @RequestParam(defaultValue = "200") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        log.info("Batch update nilai perkuliahan - tahunAkademik: {}, prodi: {}, limit: {}, offset: {}",
                tahunAkademik, prodi, limit, offset);

        try {
            // Validasi tahun akademik dan prodi
            TahunAkademik ta = tahunAkademikDao.findById(tahunAkademik).orElse(null);
            Prodi p = prodiDao.findById(prodi).orElse(null);

            if (ta == null || p == null) {
                return ResponseEntity.badRequest().body(SyncResult.<String>builder()
                        .entityName("NilaiPerkuliahan")
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
                        .entityName("NilaiPerkuliahan")
                        .matchedCount(0)
                        .mismatchedCount(0)
                        .build());
            }

            // ============================================
            // BATCH OPTIMIZATION: Collect all data upfront
            // WITH PAGINATION (limit & offset)
            // ============================================

            // 1. Collect ALL ready-to-sync KRS from ALL jadwals first
            List<KrsDetail> allReadyToSync = new ArrayList<>();

            for (Jadwal jadwal : jadwals) {
                List<KrsDetail> pesertaList = krsDetailDao.findByJadwalAndStatus(jadwal, StatusRecord.AKTIF);
                pesertaList.stream()
                        .filter(KrsDetail::isReadyForNilaiSync)
                        .filter(krs -> krs.getMahasiswa() != null)
                        .filter(krs -> krs.getMahasiswa().getNim() != null
                                && !krs.getMahasiswa().getNim().trim().isEmpty())
                        .forEach(allReadyToSync::add);
            }

            int totalReadyToSync = allReadyToSync.size();
            log.info("Total ready to sync: {}", totalReadyToSync);

            // 2. Apply offset and limit
            if (offset >= totalReadyToSync) {
                return ResponseEntity.ok(SyncResult.<String>builder()
                        .entityName("NilaiPerkuliahan")
                        .matchedCount(0)
                        .mismatchedCount(0)
                        .skippedCount(0)
                        .hasMore(false)
                        .totalData(totalReadyToSync)
                        .build());
            }

            int effectiveLimit = Math.min(limit, DEFAULT_BATCH_LIMIT); // Cap at max limit
            List<KrsDetail> toProcess = allReadyToSync.stream()
                    .skip(offset)
                    .limit(effectiveLimit)
                    .collect(Collectors.toList());

            boolean hasMore = (offset + toProcess.size()) < totalReadyToSync;
            int nextOffset = offset + toProcess.size();

            log.info("Processing batch: offset={}, limit={}, toProcess={}, hasMore={}",
                    offset, effectiveLimit, toProcess.size(), hasMore);

            // 3. Collect unique NIMs from this batch only
            List<String> batchNims = toProcess.stream()
                    .map(krs -> krs.getMahasiswa().getNim().trim())
                    .distinct()
                    .collect(Collectors.toList());

            // 4. Batch fetch id_registrasi_mahasiswa for this batch
            log.info("Batch fetching {} mahasiswa from Feeder", batchNims.size());
            Map<String, String> mahasiswaCache = batchNims.isEmpty()
                    ? new HashMap<>()
                    : feederService.getAllMahasiswaByNims(batchNims);
            log.info("Found {} mahasiswa in Feeder", mahasiswaCache.size());

            // ============================================

            int successCount = 0;
            int failedCount = 0;
            int skippedCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (KrsDetail krs : toProcess) {
                try {
                    String idKelasKuliah = krs.getJadwal().getIdKelasKuliahFeeder();
                    String nim = krs.getMahasiswa().getNim().trim();

                    // Get id_registrasi_mahasiswa dari cache
                    String idRegistrasiMahasiswa = mahasiswaCache.get(nim);
                    if (idRegistrasiMahasiswa == null) {
                        log.debug("Mahasiswa {} not found in Feeder cache", nim);
                        skippedCount++;
                        continue;
                    }

                    // Prepare nilai
                    Double nilaiAngka = krs.getNilaiAkhir() != null
                            ? krs.getNilaiAkhir().doubleValue()
                            : 0.0;
                    Double nilaiIndeks = krs.getBobot() != null
                            ? krs.getBobot().doubleValue()
                            : 0.0;
                    String nilaiHuruf = krs.getGrade();

                    // Update nilai ke Feeder
                    FeederResponse<Object> response = feederService.updateNilaiPerkuliahanKelas(
                            idRegistrasiMahasiswa,
                            idKelasKuliah,
                            nilaiAngka,
                            nilaiIndeks,
                            nilaiHuruf);

                    if (response.isSuccess()) {
                        // Update status lokal
                        krs.setStatusNilaiFeeder(StatusRecord.AKTIF);
                        krsDetailDao.save(krs);
                        successCount++;
                        log.debug("Nilai updated successfully - krs: {}, mhs: {}", krs.getId(), nim);
                    } else {
                        failedCount++;
                        String errorMsg = String.format("Mhs %s: %s", nim, response.getErrorDesc());
                        if (errorMessages.size() < 10) {
                            errorMessages.add(errorMsg);
                        }
                        log.warn("Failed to update nilai for krs: {} - {}", krs.getId(), response.getErrorDesc());
                    }

                } catch (Exception e) {
                    failedCount++;
                    log.error("Error updating nilai for krs: {}", krs.getId(), e);
                    if (errorMessages.size() < 10) {
                        errorMessages.add("Error: " + e.getMessage());
                    }
                }
            }

            log.info("Batch update nilai completed - success: {}, failed: {}, skipped: {}, hasMore: {}",
                    successCount, failedCount, skippedCount, hasMore);

            return ResponseEntity.ok(SyncResult.<String>builder()
                    .entityName("NilaiPerkuliahan")
                    .matchedCount(successCount)
                    .mismatchedCount(failedCount)
                    .skippedCount(skippedCount)
                    .hasMore(hasMore)
                    .totalData(totalReadyToSync)
                    .nextOffset(nextOffset)
                    .errors(errorMessages)
                    .build());

        } catch (Exception e) {
            log.error("Error in batch update nilai perkuliahan", e);
            return ResponseEntity.internalServerError().body(SyncResult.<String>builder()
                    .entityName("NilaiPerkuliahan")
                    .errors(List.of("Internal server error: " + e.getMessage()))
                    .build());
        }
    }

    /**
     * Update nilai perkuliahan per jadwal
     * POST /api/feeder/update/nilai-perkuliahan/jadwal?jadwalId=xxx
     */
    @PostMapping("/update/nilai-perkuliahan/jadwal")
    public ResponseEntity<SyncResult<String>> updateNilaiPerJadwal(
            @RequestParam String jadwalId) {

        log.info("Update nilai perkuliahan per jadwal - jadwalId: {}", jadwalId);

        try {
            // Get jadwal
            Jadwal jadwal = jadwalDao.findById(jadwalId).orElse(null);

            if (jadwal == null) {
                return ResponseEntity.badRequest().body(SyncResult.<String>builder()
                        .entityName("NilaiPerkuliahan")
                        .errors(List.of("Jadwal tidak ditemukan"))
                        .build());
            }

            if (!jadwal.isKelasSyncedToFeeder()) {
                return ResponseEntity.badRequest().body(SyncResult.<String>builder()
                        .entityName("NilaiPerkuliahan")
                        .errors(List.of("Jadwal belum ter-sync ke Feeder. Silakan sync kelas kuliah terlebih dahulu."))
                        .build());
            }

            String idKelasKuliah = jadwal.getIdKelasKuliahFeeder();

            // Get peserta
            List<KrsDetail> pesertaList = krsDetailDao.findByJadwalAndStatus(jadwal, StatusRecord.AKTIF);

            if (pesertaList.isEmpty()) {
                return ResponseEntity.ok(SyncResult.<String>builder()
                        .entityName("NilaiPerkuliahan")
                        .matchedCount(0)
                        .mismatchedCount(0)
                        .build());
            }

            // Collect NIMs untuk batch fetch
            List<String> nims = pesertaList.stream()
                    .filter(KrsDetail::isReadyForNilaiSync)
                    .filter(krs -> krs.getMahasiswa() != null && krs.getMahasiswa().getNim() != null)
                    .map(krs -> krs.getMahasiswa().getNim().trim())
                    .distinct()
                    .collect(Collectors.toList());

            // Batch fetch id_registrasi_mahasiswa
            Map<String, String> mahasiswaCache = nims.isEmpty()
                    ? new HashMap<>()
                    : feederService.getAllMahasiswaByNims(nims);

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

                    // Skip jika tidak ready untuk sync
                    if (!krs.isReadyForNilaiSync()) {
                        skippedCount++;
                        continue;
                    }

                    String nim = krs.getMahasiswa().getNim();
                    if (nim == null || nim.trim().isEmpty()) {
                        skippedCount++;
                        continue;
                    }

                    // Get id_registrasi_mahasiswa dari cache
                    String idRegistrasiMahasiswa = mahasiswaCache.get(nim.trim());
                    if (idRegistrasiMahasiswa == null) {
                        skippedCount++;
                        continue;
                    }

                    // Prepare nilai
                    Double nilaiAngka = krs.getNilaiAkhir() != null
                            ? krs.getNilaiAkhir().doubleValue()
                            : 0.0;
                    Double nilaiIndeks = krs.getBobot() != null
                            ? krs.getBobot().doubleValue()
                            : 0.0;
                    String nilaiHuruf = krs.getGrade();

                    // Update nilai ke Feeder
                    FeederResponse<Object> response = feederService.updateNilaiPerkuliahanKelas(
                            idRegistrasiMahasiswa,
                            idKelasKuliah,
                            nilaiAngka,
                            nilaiIndeks,
                            nilaiHuruf);

                    if (response.isSuccess()) {
                        // Update status lokal
                        krs.setStatusNilaiFeeder(StatusRecord.AKTIF);
                        krsDetailDao.save(krs);
                        successCount++;
                    } else {
                        failedCount++;
                        if (errorMessages.size() < 10) {
                            errorMessages.add(String.format("Mhs %s: %s", nim, response.getErrorDesc()));
                        }
                    }

                } catch (Exception e) {
                    failedCount++;
                    log.error("Error updating nilai for krs: {}", krs.getId(), e);
                    if (errorMessages.size() < 10) {
                        errorMessages.add("Error: " + e.getMessage());
                    }
                }
            }

            log.info("Update nilai per jadwal completed - success: {}, failed: {}, skipped: {}",
                    successCount, failedCount, skippedCount);

            return ResponseEntity.ok(SyncResult.<String>builder()
                    .entityName("NilaiPerkuliahan")
                    .matchedCount(successCount)
                    .mismatchedCount(failedCount)
                    .skippedCount(skippedCount)
                    .errors(errorMessages)
                    .build());

        } catch (Exception e) {
            log.error("Error in update nilai per jadwal", e);
            return ResponseEntity.internalServerError().body(SyncResult.<String>builder()
                    .entityName("NilaiPerkuliahan")
                    .errors(List.of("Internal server error: " + e.getMessage()))
                    .build());
        }
    }

    /**
     * Get detail nilai dari Feeder untuk jadwal tertentu
     * GET /api/feeder/nilai-perkuliahan/detail?jadwalId=xxx
     */
    @GetMapping("/nilai-perkuliahan/detail")
    public ResponseEntity<?> getNilaiDetail(@RequestParam String jadwalId) {
        log.info("Get nilai detail - jadwalId: {}", jadwalId);

        try {
            Jadwal jadwal = jadwalDao.findById(jadwalId).orElse(null);

            if (jadwal == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Jadwal tidak ditemukan"));
            }

            if (!jadwal.isKelasSyncedToFeeder()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Jadwal belum ter-sync ke Feeder"));
            }

            // Get nilai dari Feeder
            List<Map<String, Object>> nilaiFeeder = feederService
                    .getDetailNilaiPerkuliahanKelas(jadwal.getIdKelasKuliahFeeder());

            // Get peserta lokal
            List<KrsDetail> pesertaLokal = krsDetailDao.findByJadwalAndStatus(jadwal, StatusRecord.AKTIF);

            // Build comparison data
            List<Map<String, Object>> comparisonData = new ArrayList<>();

            for (KrsDetail krs : pesertaLokal) {
                if (krs.getMahasiswa() == null)
                    continue;

                Map<String, Object> row = new HashMap<>();
                row.put("krsDetailId", krs.getId());
                row.put("nim", krs.getMahasiswa().getNim());
                row.put("nama", krs.getMahasiswa().getNama());
                row.put("nilaiLokal", Map.of(
                        "nilaiAkhir", krs.getNilaiAkhir(),
                        "bobot", krs.getBobot(),
                        "grade", krs.getGrade(),
                        "finalisasi", krs.getFinalisasi(),
                        "statusFeeder", krs.getStatusFeeder(),
                        "statusNilaiFeeder", krs.getStatusNilaiFeeder()));

                // Find matching nilai in Feeder
                String nim = krs.getMahasiswa().getNim();
                Map<String, Object> nilaiFromFeeder = nilaiFeeder.stream()
                        .filter(n -> nim.equals(n.get("nim")))
                        .findFirst()
                        .orElse(null);

                if (nilaiFromFeeder != null) {
                    row.put("nilaiFeeder", Map.of(
                            "nilai_angka", nilaiFromFeeder.get("nilai_angka"),
                            "nilai_indeks", nilaiFromFeeder.get("nilai_indeks"),
                            "nilai_huruf", nilaiFromFeeder.get("nilai_huruf")));
                    row.put("syncStatus", "SYNCED");
                } else {
                    row.put("nilaiFeeder", null);
                    row.put("syncStatus", krs.isSyncedToFeeder() ? "NOT_SYNCED" : "PESERTA_NOT_SYNCED");
                }

                comparisonData.add(row);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "jadwal", Map.of(
                            "id", jadwal.getId(),
                            "matakuliah", jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah(),
                            "kelas", jadwal.getKelas().getNamaKelas(),
                            "idKelasKuliahFeeder", jadwal.getIdKelasKuliahFeeder()),
                    "totalPeserta", pesertaLokal.size(),
                    "totalNilaiFeeder", nilaiFeeder.size(),
                    "data", comparisonData));

        } catch (Exception e) {
            log.error("Error getting nilai detail", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage()));
        }
    }
}
