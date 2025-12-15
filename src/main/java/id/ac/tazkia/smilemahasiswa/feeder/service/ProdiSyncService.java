package id.ac.tazkia.smilemahasiswa.feeder.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederProdiDto;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import id.ac.tazkia.smilemahasiswa.feeder.dto.SyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service untuk sinkronisasi data Prodi antara database lokal dan PDDIKTI
 * Feeder
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProdiSyncService {

    private final FeederService feederService;
    private final ProdiDao prodiDao;
    private final ObjectMapper objectMapper;

    /**
     * Cek status sinkronisasi Prodi antara database lokal dan Feeder
     * 
     * @return SyncResult dengan detail perbandingan
     */
    public SyncResult<Map<String, Object>> checkSync() {
        log.info("Checking Prodi synchronization status...");

        SyncResult<Map<String, Object>> result = SyncResult.<Map<String, Object>>builder()
                .entityName("Prodi")
                .syncTime(LocalDateTime.now())
                .matched(new ArrayList<>())
                .mismatched(new ArrayList<>())
                .missingInFeeder(new ArrayList<>())
                .missingInLocal(new ArrayList<>())
                .build();

        // 1. Ambil semua Prodi AKTIF dari database lokal yang punya kodePddikti
        List<Prodi> localProdiList = prodiDao.findByStatus(StatusRecord.AKTIF).stream()
                .filter(p -> p.getKodePddikti() != null && !p.getKodePddikti().isEmpty())
                .collect(Collectors.toList());

        result.setTotalLocal(localProdiList.size());
        log.info("Found {} Prodi with kodePddikti in local database", localProdiList.size());

        // 2. Ambil semua Prodi dari Feeder
        FeederResponse<Object> feederResponse = feederService.execute("GetProdi");

        if (!feederResponse.isSuccess()) {
            log.error("Failed to get Prodi from Feeder: {}", feederResponse.getErrorDesc());
            throw new RuntimeException("Failed to get Prodi from Feeder: " + feederResponse.getErrorDesc());
        }

        List<FeederProdiDto> feederProdiList = convertToFeederProdiList(feederResponse.getData());
        result.setTotalFeeder(feederProdiList.size());
        log.info("Found {} Prodi in Feeder", feederProdiList.size());

        // 3. Buat map untuk lookup cepat
        // Lokal: kodePddikti berisi kode program studi (misal: 61206)
        // Feeder: kode_program_studi berisi kode yang sama
        Map<String, Prodi> localProdiMap = localProdiList.stream()
                .collect(Collectors.toMap(Prodi::getKodePddikti, p -> p, (a, b) -> a));

        Map<String, FeederProdiDto> feederProdiMap = feederProdiList.stream()
                .collect(Collectors.toMap(FeederProdiDto::getKodeProgramStudi, p -> p, (a, b) -> a));

        // 4. Bandingkan data
        for (Prodi local : localProdiList) {
            String kodePddikti = local.getKodePddikti();
            FeederProdiDto feeder = feederProdiMap.get(kodePddikti);

            if (feeder == null) {
                // Ada di lokal tapi tidak ada di Feeder
                result.getMissingInFeeder().add(convertProdiToMap(local));
            } else {
                // Ada di keduanya, bandingkan
                List<String> differences = compareProdi(local, feeder);

                SyncResult.SyncItem<Map<String, Object>> item = SyncResult.SyncItem.<Map<String, Object>>builder()
                        .local(convertProdiToMap(local))
                        .feeder(convertFeederProdiToMap(feeder))
                        .differences(differences)
                        .build();

                if (differences.isEmpty()) {
                    result.getMatched().add(item);
                } else {
                    result.getMismatched().add(item);
                }
            }
        }

        // 5. Cari yang ada di Feeder tapi tidak ada di lokal
        for (FeederProdiDto feeder : feederProdiList) {
            if (!localProdiMap.containsKey(feeder.getKodeProgramStudi())) {
                result.getMissingInLocal().add(convertFeederProdiToMap(feeder));
            }
        }

        // 6. Set counts
        result.setMatchedCount(result.getMatched().size());
        result.setMismatchedCount(result.getMismatched().size());
        result.setMissingInFeederCount(result.getMissingInFeeder().size());
        result.setMissingInLocalCount(result.getMissingInLocal().size());

        log.info("Sync check completed: {}", result.getSummary());

        return result;
    }

    /**
     * Get daftar Prodi lokal yang belum ada di Feeder
     */
    public List<Prodi> getProdiNotInFeeder() {
        List<Prodi> localProdiList = prodiDao.findByStatus(StatusRecord.AKTIF).stream()
                .filter(p -> p.getKodePddikti() != null && !p.getKodePddikti().isEmpty())
                .collect(Collectors.toList());

        FeederResponse<Object> feederResponse = feederService.execute("GetProdi");
        if (!feederResponse.isSuccess()) {
            throw new RuntimeException("Failed to get Prodi from Feeder");
        }

        List<FeederProdiDto> feederProdiList = convertToFeederProdiList(feederResponse.getData());
        Set<String> feederKodes = feederProdiList.stream()
                .map(FeederProdiDto::getKodeProgramStudi)
                .collect(Collectors.toSet());

        return localProdiList.stream()
                .filter(p -> !feederKodes.contains(p.getKodePddikti()))
                .collect(Collectors.toList());
    }

    /**
     * Get daftar Prodi di Feeder yang belum ada di lokal
     */
    public List<FeederProdiDto> getProdiNotInLocal() {
        List<Prodi> localProdiList = prodiDao.findByStatus(StatusRecord.AKTIF).stream()
                .filter(p -> p.getKodePddikti() != null && !p.getKodePddikti().isEmpty())
                .collect(Collectors.toList());

        Set<String> localIds = localProdiList.stream()
                .map(Prodi::getKodePddikti)
                .collect(Collectors.toSet());

        FeederResponse<Object> feederResponse = feederService.execute("GetProdi");
        if (!feederResponse.isSuccess()) {
            throw new RuntimeException("Failed to get Prodi from Feeder");
        }

        List<FeederProdiDto> feederProdiList = convertToFeederProdiList(feederResponse.getData());

        return feederProdiList.stream()
                .filter(p -> !localIds.contains(p.getKodeProgramStudi()))
                .collect(Collectors.toList());
    }

    /**
     * Bandingkan data Prodi lokal dengan Feeder
     * 
     * Mapping field:
     * - kodePddikti (lokal) ↔ kode_program_studi (Feeder)
     * - namaProdi (lokal) ↔ nama_program_studi (Feeder)
     * - status (lokal) ↔ status (Feeder): AKTIF ↔ "A"
     * 
     * Note: kodeProdi lokal (misal "MBS") adalah kode internal, TIDAK dibandingkan
     * dengan Feeder
     */
    private List<String> compareProdi(Prodi local, FeederProdiDto feeder) {
        List<String> differences = new ArrayList<>();

        // Bandingkan nama (dengan normalisasi)
        String localNama = normalizeString(local.getNamaProdi());
        String feederNama = normalizeString(feeder.getNamaProgramStudi());

        if (!localNama.equalsIgnoreCase(feederNama)) {
            differences.add(
                    String.format("Nama berbeda: '%s' vs '%s'", local.getNamaProdi(), feeder.getNamaProgramStudi()));
        }

        // Bandingkan kodePddikti lokal dengan kode_program_studi Feeder
        // (Seharusnya sama karena kita matching berdasarkan ini, tapi double check)
        if (local.getKodePddikti() != null && feeder.getKodeProgramStudi() != null) {
            if (!local.getKodePddikti().equals(feeder.getKodeProgramStudi())) {
                differences.add(String.format("Kode PDDIKTI berbeda: '%s' vs '%s'",
                        local.getKodePddikti(), feeder.getKodeProgramStudi()));
            }
        }

        // Bandingkan status
        String feederStatus = feeder.getStatus();
        StatusRecord localStatus = local.getStatus();
        if (feederStatus != null && localStatus != null) {
            boolean feederAktif = "A".equalsIgnoreCase(feederStatus);
            boolean localAktif = localStatus == StatusRecord.AKTIF;
            if (feederAktif != localAktif) {
                differences.add(String.format("Status berbeda: '%s' vs '%s'", localStatus, feederStatus));
            }
        }

        return differences;
    }

    private String normalizeString(String s) {
        if (s == null)
            return "";
        return s.trim().replaceAll("\\s+", " ");
    }

    @SuppressWarnings("unchecked")
    private List<FeederProdiDto> convertToFeederProdiList(Object data) {
        List<FeederProdiDto> result = new ArrayList<>();

        if (data instanceof List) {
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) data;
            for (Map<String, Object> item : dataList) {
                FeederProdiDto dto = objectMapper.convertValue(item, FeederProdiDto.class);
                result.add(dto);
            }
        }

        return result;
    }

    private Map<String, Object> convertProdiToMap(Prodi prodi) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", prodi.getId());
        map.put("kodePddikti", prodi.getKodePddikti());
        map.put("kodeProdi", prodi.getKodeProdi());
        map.put("namaProdi", prodi.getNamaProdi());
        map.put("status", prodi.getStatus() != null ? prodi.getStatus().name() : null);
        map.put("jenjang", prodi.getIdJenjang() != null ? prodi.getIdJenjang().getNamaJenjang() : null);
        return map;
    }

    private Map<String, Object> convertFeederProdiToMap(FeederProdiDto feeder) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id_prodi", feeder.getIdProdi());
        map.put("kode_program_studi", feeder.getKodeProgramStudi());
        map.put("nama_program_studi", feeder.getNamaProgramStudi());
        map.put("status", feeder.getStatus());
        map.put("jenjang", feeder.getNamaJenjangPendidikan());
        return map;
    }
}
