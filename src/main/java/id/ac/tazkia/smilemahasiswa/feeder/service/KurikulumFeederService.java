package id.ac.tazkia.smilemahasiswa.feeder.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import id.ac.tazkia.smilemahasiswa.dao.KurikulumDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.entity.Kurikulum;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederKurikulumDto;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import id.ac.tazkia.smilemahasiswa.feeder.dto.SyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service untuk mendapatkan dan sinkronisasi data Kurikulum dari PDDIKTI Feeder
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KurikulumFeederService {

    private final FeederService feederService;
    private final ProdiDao prodiDao;
    private final KurikulumDao kurikulumDao;
    private final ObjectMapper objectMapper;

    /**
     * Get semua kurikulum dari Feeder berdasarkan Prodi
     * 
     * @param prodi Prodi lokal (harus punya kodePddikti)
     * @return List of FeederKurikulumDto
     */
    public List<FeederKurikulumDto> getKurikulumByProdi(Prodi prodi) {
        if (prodi == null || prodi.getKodePddikti() == null || prodi.getKodePddikti().isEmpty()) {
            log.warn("Prodi atau kodePddikti tidak valid");
            return Collections.emptyList();
        }

        // Gunakan kode_program_studi untuk filter, bukan id_prodi
        // Karena di Feeder, id_prodi adalah UUID sedangkan kita punya
        // kode_program_studi
        String filter = "id_prodi = '" + getIdProdiFeeder(prodi) + "'";

        log.info("Getting kurikulum from Feeder with filter: {}", filter);

        FeederResponse<Object> response = feederService.execute("GetListKurikulum", filter);

        if (!response.isSuccess()) {
            log.error("Failed to get kurikulum from Feeder: {}", response.getErrorDesc());
            throw new RuntimeException("Failed to get kurikulum from Feeder: " + response.getErrorDesc());
        }

        return convertToKurikulumList(response.getData());
    }

    /**
     * Get id_prodi dari Feeder berdasarkan Prodi lokal
     * Karena kita perlu UUID id_prodi dari Feeder, bukan kode_program_studi
     * 
     * @param prodi Prodi lokal
     * @return id_prodi dari Feeder (UUID)
     */
    public String getIdProdiFeeder(Prodi prodi) {
        if (prodi == null || prodi.getKodePddikti() == null) {
            return null;
        }

        // Filter GetProdi berdasarkan kode_program_studi
        String filter = "kode_program_studi = '" + prodi.getKodePddikti() + "'";

        FeederResponse<Object> response = feederService.execute("GetProdi", filter);

        if (!response.isSuccess() || response.getData() == null) {
            log.warn("Prodi tidak ditemukan di Feeder dengan kode: {}", prodi.getKodePddikti());
            return null;
        }

        List<Map<String, Object>> dataList = convertToMapList(response.getData());
        if (dataList.isEmpty()) {
            return null;
        }

        // Ambil id_prodi dari hasil pertama
        return (String) dataList.get(0).get("id_prodi");
    }

    /**
     * Cari kurikulum di Feeder berdasarkan Prodi dan nama kurikulum
     * 
     * @param prodi         Prodi lokal
     * @param namaKurikulum nama kurikulum yang dicari
     * @return FeederKurikulumDto jika ditemukan, null jika tidak
     */
    public FeederKurikulumDto findByProdiAndNama(Prodi prodi, String namaKurikulum) {
        List<FeederKurikulumDto> kurikulumList = getKurikulumByProdi(prodi);

        return kurikulumList.stream()
                .filter(k -> k.getNamaKurikulum() != null &&
                        k.getNamaKurikulum().equalsIgnoreCase(namaKurikulum))
                .findFirst()
                .orElse(null);
    }

    /**
     * Cari kurikulum di Feeder berdasarkan Kurikulum lokal
     * Match berdasarkan nama kurikulum
     * 
     * @param kurikulumLokal Kurikulum dari database lokal
     * @return FeederKurikulumDto jika ditemukan, null jika tidak
     */
    public FeederKurikulumDto findByKurikulumLokal(Kurikulum kurikulumLokal) {
        if (kurikulumLokal == null || kurikulumLokal.getProdi() == null) {
            return null;
        }

        // Jika sudah punya idPddikti, cari langsung by id
        if (kurikulumLokal.getIdPddikti() != null && !kurikulumLokal.getIdPddikti().isEmpty()) {
            return findByIdKurikulum(kurikulumLokal.getIdPddikti());
        }

        // Jika belum, cari by nama
        return findByProdiAndNama(kurikulumLokal.getProdi(), kurikulumLokal.getNamaKurikulum());
    }

    /**
     * Cari kurikulum di Feeder berdasarkan id_kurikulum
     * 
     * @param idKurikulum ID kurikulum dari Feeder (UUID)
     * @return FeederKurikulumDto jika ditemukan, null jika tidak
     */
    public FeederKurikulumDto findByIdKurikulum(String idKurikulum) {
        if (idKurikulum == null || idKurikulum.isEmpty()) {
            return null;
        }

        String filter = "id_kurikulum = '" + idKurikulum + "'";
        FeederResponse<Object> response = feederService.execute("GetListKurikulum", filter);

        if (!response.isSuccess() || response.getData() == null) {
            return null;
        }

        List<FeederKurikulumDto> list = convertToKurikulumList(response.getData());
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Get id_kurikulum dari Feeder berdasarkan Kurikulum lokal
     * Jika kurikulum sudah punya idPddikti, langsung return tanpa query
     * 
     * @param kurikulumLokal Kurikulum dari database lokal
     * @return id_kurikulum dari Feeder (UUID), null jika tidak ditemukan
     */
    public String getIdKurikulumFeeder(Kurikulum kurikulumLokal) {
        // Jika sudah ada idPddikti, langsung return
        if (kurikulumLokal.getIdPddikti() != null && !kurikulumLokal.getIdPddikti().isEmpty()) {
            return kurikulumLokal.getIdPddikti();
        }

        // Jika belum, cari di Feeder
        FeederKurikulumDto feederKurikulum = findByKurikulumLokal(kurikulumLokal);
        return feederKurikulum != null ? feederKurikulum.getIdKurikulum() : null;
    }

    /**
     * Sync idPddikti untuk semua kurikulum di prodi tertentu
     * 
     * @param prodi Prodi yang kurikulumnya akan di-sync
     * @return Map dengan hasil sync (updated, notFound, alreadySynced)
     */
    public Map<String, Object> syncIdPddikti(Prodi prodi) {
        log.info("Syncing idPddikti for kurikulum in Prodi: {}", prodi.getNamaProdi());

        List<Kurikulum> kurikulumList = kurikulumDao.findByProdiAndStatusNotIn(
                prodi, Arrays.asList(StatusRecord.HAPUS));

        List<FeederKurikulumDto> feederList = getKurikulumByProdi(prodi);

        // Map feeder by nama (normalized)
        Map<String, FeederKurikulumDto> feederMap = feederList.stream()
                .filter(k -> k.getNamaKurikulum() != null)
                .collect(Collectors.toMap(
                        k -> normalizeString(k.getNamaKurikulum()),
                        k -> k,
                        (a, b) -> a));

        int updated = 0;
        int notFound = 0;
        int alreadySynced = 0;
        List<String> updatedList = new ArrayList<>();
        List<String> notFoundList = new ArrayList<>();

        for (Kurikulum k : kurikulumList) {
            if (k.getIdPddikti() != null && !k.getIdPddikti().isEmpty()) {
                alreadySynced++;
                continue;
            }

            String key = normalizeString(k.getNamaKurikulum());
            FeederKurikulumDto feeder = feederMap.get(key);

            if (feeder != null) {
                k.setIdPddikti(feeder.getIdKurikulum());
                kurikulumDao.save(k);
                updated++;
                updatedList.add(k.getNamaKurikulum() + " -> " + feeder.getIdKurikulum());
            } else {
                notFound++;
                notFoundList.add(k.getNamaKurikulum());
            }
        }

        log.info("Sync completed - updated: {}, notFound: {}, alreadySynced: {}",
                updated, notFound, alreadySynced);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("prodi", prodi.getNamaProdi());
        result.put("total", kurikulumList.size());
        result.put("updated", updated);
        result.put("notFound", notFound);
        result.put("alreadySynced", alreadySynced);
        result.put("updatedList", updatedList);
        result.put("notFoundList", notFoundList);

        return result;
    }

    /**
     * Cek sinkronisasi Kurikulum per Prodi antara database lokal dan Feeder
     */
    public SyncResult<Map<String, Object>> checkSync(Prodi prodi) {
        log.info("Checking Kurikulum synchronization for Prodi: {}", prodi.getNamaProdi());

        SyncResult<Map<String, Object>> result = SyncResult.<Map<String, Object>>builder()
                .entityName("Kurikulum - " + prodi.getNamaProdi())
                .syncTime(LocalDateTime.now())
                .matched(new ArrayList<>())
                .mismatched(new ArrayList<>())
                .missingInFeeder(new ArrayList<>())
                .missingInLocal(new ArrayList<>())
                .build();

        // 1. Ambil kurikulum lokal AKTIF untuk prodi ini
        List<Kurikulum> localKurikulumList = kurikulumDao.findByProdiAndStatusNotIn(
                prodi, Arrays.asList(StatusRecord.HAPUS));
        result.setTotalLocal(localKurikulumList.size());

        // 2. Ambil kurikulum dari Feeder
        List<FeederKurikulumDto> feederKurikulumList = getKurikulumByProdi(prodi);
        result.setTotalFeeder(feederKurikulumList.size());

        // 3. Buat map untuk lookup (by nama kurikulum - normalized)
        Map<String, Kurikulum> localMap = localKurikulumList.stream()
                .filter(k -> k.getNamaKurikulum() != null)
                .collect(Collectors.toMap(
                        k -> normalizeString(k.getNamaKurikulum()),
                        k -> k,
                        (a, b) -> a));

        Map<String, FeederKurikulumDto> feederMap = feederKurikulumList.stream()
                .filter(k -> k.getNamaKurikulum() != null)
                .collect(Collectors.toMap(
                        k -> normalizeString(k.getNamaKurikulum()),
                        k -> k,
                        (a, b) -> a));

        // 4. Bandingkan
        for (Kurikulum local : localKurikulumList) {
            String key = normalizeString(local.getNamaKurikulum());
            FeederKurikulumDto feeder = feederMap.get(key);

            if (feeder == null) {
                result.getMissingInFeeder().add(convertKurikulumToMap(local));
            } else {
                List<String> differences = compareKurikulum(local, feeder);
                SyncResult.SyncItem<Map<String, Object>> item = SyncResult.SyncItem.<Map<String, Object>>builder()
                        .local(convertKurikulumToMap(local))
                        .feeder(convertFeederKurikulumToMap(feeder))
                        .differences(differences)
                        .build();

                if (differences.isEmpty()) {
                    result.getMatched().add(item);
                } else {
                    result.getMismatched().add(item);
                }
            }
        }

        // 5. Cari yang ada di Feeder tapi tidak di lokal
        for (FeederKurikulumDto feeder : feederKurikulumList) {
            String key = normalizeString(feeder.getNamaKurikulum());
            if (!localMap.containsKey(key)) {
                result.getMissingInLocal().add(convertFeederKurikulumToMap(feeder));
            }
        }

        // 6. Set counts
        result.setMatchedCount(result.getMatched().size());
        result.setMismatchedCount(result.getMismatched().size());
        result.setMissingInFeederCount(result.getMissingInFeeder().size());
        result.setMissingInLocalCount(result.getMissingInLocal().size());

        log.info("Kurikulum sync check completed: {}", result.getSummary());

        return result;
    }

    private List<String> compareKurikulum(Kurikulum local, FeederKurikulumDto feeder) {
        List<String> differences = new ArrayList<>();

        // Bandingkan nama
        if (!normalizeString(local.getNamaKurikulum()).equalsIgnoreCase(
                normalizeString(feeder.getNamaKurikulum()))) {
            differences.add(String.format("Nama berbeda: '%s' vs '%s'",
                    local.getNamaKurikulum(), feeder.getNamaKurikulum()));
        }

        return differences;
    }

    private String normalizeString(String s) {
        if (s == null)
            return "";
        return s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    @SuppressWarnings("unchecked")
    private List<FeederKurikulumDto> convertToKurikulumList(Object data) {
        List<FeederKurikulumDto> result = new ArrayList<>();

        if (data instanceof List) {
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) data;
            for (Map<String, Object> item : dataList) {
                FeederKurikulumDto dto = objectMapper.convertValue(item, FeederKurikulumDto.class);
                result.add(dto);
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> convertToMapList(Object data) {
        if (data instanceof List) {
            return (List<Map<String, Object>>) data;
        }
        return Collections.emptyList();
    }

    private Map<String, Object> convertKurikulumToMap(Kurikulum kurikulum) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", kurikulum.getId());
        map.put("namaKurikulum", kurikulum.getNamaKurikulum());
        map.put("tahunKurikulum", kurikulum.getTahunKurikulum());
        map.put("status", kurikulum.getStatus() != null ? kurikulum.getStatus().name() : null);
        map.put("prodi", kurikulum.getProdi() != null ? kurikulum.getProdi().getNamaProdi() : null);
        return map;
    }

    private Map<String, Object> convertFeederKurikulumToMap(FeederKurikulumDto feeder) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id_kurikulum", feeder.getIdKurikulum());
        map.put("nama_kurikulum", feeder.getNamaKurikulum());
        map.put("id_semester", feeder.getIdSemester());
        map.put("nama_semester", feeder.getNamaSemester());
        map.put("jumlah_sks_lulus", feeder.getJumlahSksLulus());
        map.put("nama_program_studi", feeder.getNamaProgramStudi());
        return map;
    }
}
