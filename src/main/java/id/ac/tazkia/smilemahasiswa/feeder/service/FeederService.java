package id.ac.tazkia.smilemahasiswa.feeder.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import id.ac.tazkia.smilemahasiswa.feeder.client.FeederApiClient;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederRequest;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Main service for interacting with PDDIKTI Feeder API.
 * Combines authentication and API client functionality.
 * Automatically handles token management and retry on token expiry.
 */
@Slf4j
@Service
public class FeederService {

    private final FeederAuthService authService;
    private final FeederApiClient apiClient;

    public FeederService(FeederAuthService authService, FeederApiClient apiClient) {
        this.authService = authService;
        this.apiClient = apiClient;
    }

    /**
     * Execute a Feeder API call with automatic token management.
     *
     * @param act the API function name
     * @return the response from the API
     */
    public FeederResponse<Object> execute(String act) {
        return execute(act, null, null, null, null);
    }

    /**
     * Execute a Feeder API call with filter.
     *
     * @param act    the API function name
     * @param filter the filter condition
     * @return the response from the API
     */
    public FeederResponse<Object> execute(String act, String filter) {
        return execute(act, filter, null, null, null);
    }

    /**
     * Execute a Feeder API call with pagination.
     *
     * @param act    the API function name
     * @param filter the filter condition
     * @param order  the order clause
     * @param limit  the maximum number of records
     * @param offset the offset for pagination
     * @return the response from the API
     */
    public FeederResponse<Object> execute(String act, String filter, String order,
            Integer limit, Integer offset) {
        String token = authService.getToken();
        if (token == null) {
            log.error("Unable to obtain Feeder token");
            return createErrorResponse(-1, "Unable to obtain authentication token");
        }

        FeederRequest request = FeederRequest.createGetRequest(act, token, filter, order, limit, offset);
        FeederResponse<Object> response = apiClient.send(request);

        // Retry with fresh token if token expired
        if (response.isTokenExpired()) {
            log.info("Token expired, refreshing and retrying...");
            token = authService.forceRefreshToken();
            if (token == null) {
                return createErrorResponse(-1, "Unable to refresh authentication token");
            }
            request = FeederRequest.createGetRequest(act, token, filter, order, limit, offset);
            response = apiClient.send(request);
        }

        return response;
    }

    /**
     * Execute a Feeder API call and return data as a specific type.
     */
    public <T> FeederResponse<T> execute(String act, String filter, Class<T> dataType) {
        String token = authService.getToken();
        if (token == null) {
            log.error("Unable to obtain Feeder token");
            FeederResponse<T> response = new FeederResponse<>();
            response.setErrorCode(-1);
            response.setErrorDesc("Unable to obtain authentication token");
            return response;
        }

        FeederRequest request = FeederRequest.createGetRequest(act, token, filter);
        FeederResponse<T> response = apiClient.send(request, dataType);

        // Retry with fresh token if token expired
        if (response.isTokenExpired()) {
            log.info("Token expired, refreshing and retrying...");
            token = authService.forceRefreshToken();
            if (token == null) {
                response.setErrorCode(-1);
                response.setErrorDesc("Unable to refresh authentication token");
                return response;
            }
            request = FeederRequest.createGetRequest(act, token, filter);
            response = apiClient.send(request, dataType);
        }

        return response;
    }

    /**
     * Execute a Feeder API call and return data as a list.
     */
    public <T> FeederResponse<List<T>> executeForList(String act, String filter, Class<T> elementType) {
        String token = authService.getToken();
        if (token == null) {
            log.error("Unable to obtain Feeder token");
            FeederResponse<List<T>> response = new FeederResponse<>();
            response.setErrorCode(-1);
            response.setErrorDesc("Unable to obtain authentication token");
            return response;
        }

        FeederRequest request = FeederRequest.createGetRequest(act, token, filter);
        FeederResponse<List<T>> response = apiClient.sendForList(request, elementType);

        // Retry with fresh token if token expired
        if (response.isTokenExpired()) {
            log.info("Token expired, refreshing and retrying...");
            token = authService.forceRefreshToken();
            if (token == null) {
                response.setErrorCode(-1);
                response.setErrorDesc("Unable to refresh authentication token");
                return response;
            }
            request = FeederRequest.createGetRequest(act, token, filter);
            response = apiClient.sendForList(request, elementType);
        }

        return response;
    }

    /**
     * Execute an insert operation.
     */
    public FeederResponse<Object> insert(String act, Map<String, Object> record) {
        String token = authService.getToken();
        if (token == null) {
            return createErrorResponse(-1, "Unable to obtain authentication token");
        }

        FeederRequest request = FeederRequest.builder()
                .act(act)
                .token(token)
                .record(record)
                .build();

        FeederResponse<Object> response = apiClient.send(request);

        if (response.isTokenExpired()) {
            token = authService.forceRefreshToken();
            if (token == null) {
                return createErrorResponse(-1, "Unable to refresh authentication token");
            }
            request = FeederRequest.builder()
                    .act(act)
                    .token(token)
                    .record(record)
                    .build();
            response = apiClient.send(request);
        }

        return response;
    }

    /**
     * Execute an update operation.
     */
    public FeederResponse<Object> update(String act, Map<String, Object> key, Map<String, Object> record) {
        String token = authService.getToken();
        if (token == null) {
            return createErrorResponse(-1, "Unable to obtain authentication token");
        }

        FeederRequest request = FeederRequest.builder()
                .act(act)
                .token(token)
                .key(key)
                .record(record)
                .build();

        FeederResponse<Object> response = apiClient.send(request);

        if (response.isTokenExpired()) {
            token = authService.forceRefreshToken();
            if (token == null) {
                return createErrorResponse(-1, "Unable to refresh authentication token");
            }
            request = FeederRequest.builder()
                    .act(act)
                    .token(token)
                    .key(key)
                    .record(record)
                    .build();
            response = apiClient.send(request);
        }

        return response;
    }

    /**
     * Execute a delete operation.
     */
    public FeederResponse<Object> delete(String act, Map<String, Object> key) {
        String token = authService.getToken();
        if (token == null) {
            return createErrorResponse(-1, "Unable to obtain authentication token");
        }

        FeederRequest request = FeederRequest.builder()
                .act(act)
                .token(token)
                .key(key)
                .build();

        FeederResponse<Object> response = apiClient.send(request);

        if (response.isTokenExpired()) {
            token = authService.forceRefreshToken();
            if (token == null) {
                return createErrorResponse(-1, "Unable to refresh authentication token");
            }
            request = FeederRequest.builder()
                    .act(act)
                    .token(token)
                    .key(key)
                    .build();
            response = apiClient.send(request);
        }

        return response;
    }

    /**
     * Check if the Feeder connection is healthy by testing token acquisition.
     */
    public boolean isHealthy() {
        try {
            String token = authService.getToken();
            return token != null && !token.isEmpty();
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get token status information (for monitoring).
     */
    public Map<String, Object> getTokenStatus() {
        var tokenData = authService.getTokenData();
        if (tokenData == null) {
            return Map.of(
                    "hasToken", false,
                    "isValid", false);
        }
        return Map.of(
                "hasToken", true,
                "isValid", authService.isTokenValid(),
                "obtainedAt", tokenData.getObtainedAt().toString(),
                "expiresAt", tokenData.getExpiresAt().toString(),
                "isExpired", tokenData.isExpired());
    }

    /**
     * Search matakuliah di Feeder untuk keperluan InsertKelasKuliah
     * Priority:
     * 1. kode_mata_kuliah + id_prodi
     * 2. nama_mata_kuliah LIKE + id_prodi (jika step 1 tidak ada)
     *
     * @param kodeMatakuliah Kode matakuliah (exact match)
     * @param namaMatakuliah Nama matakuliah (LIKE search)
     * @param idProdiFeeder  ID Prodi di Feeder
     * @return Map dengan key: id_matkul, kode_mata_kuliah, nama_mata_kuliah (dari
     *         Feeder API)
     */
    public Map<String, Object> searchMatakuliahForKelas(String kodeMatakuliah, String namaMatakuliah,
            String idProdiFeeder) {
        log.debug("Searching matakuliah for kelas - kode: {}, nama: {}, idProdi: {}", kodeMatakuliah, namaMatakuliah,
                idProdiFeeder);

        // Step 1: Cari dengan kode_mata_kuliah + id_prodi
        if (kodeMatakuliah != null && !kodeMatakuliah.isEmpty()) {
            String filter = String.format("kode_mata_kuliah='%s' AND id_prodi='%s'",
                    kodeMatakuliah.replace("'", "''"), idProdiFeeder);

            FeederResponse<Object> response = execute("GetListMataKuliah", filter);

            if (response.isSuccess() && response.getData() != null) {
                List<?> dataList = (List<?>) response.getData();
                if (!dataList.isEmpty()) {
                    log.info("Matakuliah found by kode + prodi: {} - {}", kodeMatakuliah, idProdiFeeder);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) dataList.get(0);
                    return result;
                }
            }
        }

        // Step 2: Cari dengan nama_mata_kuliah LIKE + id_prodi
        if (namaMatakuliah != null && !namaMatakuliah.isEmpty()) {
            String escapedNama = namaMatakuliah.replace("'", "''");
            String filter = String.format("nama_mata_kuliah like '%%%s%%' AND id_prodi='%s'",
                    escapedNama, idProdiFeeder);

            FeederResponse<Object> response = execute("GetListMataKuliah", filter);

            if (response.isSuccess() && response.getData() != null) {
                List<?> dataList = (List<?>) response.getData();
                if (!dataList.isEmpty()) {
                    log.info("Matakuliah found by nama + prodi: {} - {}", namaMatakuliah, idProdiFeeder);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) dataList.get(0);
                    return result;
                }
            }
        }

        log.warn("Matakuliah not found for kode: {}, nama: {}, idProdi: {}", kodeMatakuliah, namaMatakuliah,
                idProdiFeeder);
        return null;
    }

    /**
     * Execute a Feeder API call with record (for Insert/Update operations)
     *
     * @param act    the API function name
     * @param record the record data
     * @return the response from the API
     */
    public FeederResponse<Object> executeWithRecord(String act, Map<String, Object> record) {
        String token = authService.getToken();
        if (token == null) {
            log.error("Unable to obtain Feeder token");
            return createErrorResponse(-1, "Unable to obtain authentication token");
        }

        FeederRequest request = FeederRequest.createInsertRequest(act, token, record);
        FeederResponse<Object> response = apiClient.send(request);

        // Retry with fresh token if token expired
        if (response.isTokenExpired()) {
            log.info("Token expired, forcing refresh and retrying...");
            String newToken = authService.forceRefreshToken();
            if (newToken == null) {
                log.error("Unable to refresh Feeder token");
                return createErrorResponse(-2, "Unable to refresh authentication token");
            }

            request = FeederRequest.createInsertRequest(act, newToken, record);
            response = apiClient.send(request);
        }

        return response;
    }

    /**
     * Insert kelas kuliah ke Feeder
     *
     * @param idProdiFeeder    ID Prodi dari Feeder
     * @param idSemesterFeeder ID Semester dari Feeder
     * @param idMatkulFeeder   ID Matakuliah dari Feeder
     * @param namaKelas        Nama kelas (max 5 karakter)
     * @param kapasitas        Kapasitas kelas
     * @param lingkup          1: Internal, 2: External, 3: Campuran
     * @param mode             O: Online, F: Offline, M: Campuran
     * @return FeederResponse dengan id_kelas_kuliah jika sukses
     */
    public FeederResponse<String> insertKelasKuliah(String idProdiFeeder, String idSemesterFeeder,
            String idMatkulFeeder, String namaKelas, Integer kapasitas, Integer lingkup, String mode) {

        log.info("Inserting kelas kuliah to feeder - prodi: {}, semester: {}, matkul: {}, kelas: {}",
                idProdiFeeder, idSemesterFeeder, idMatkulFeeder, namaKelas);

        // Validasi required fields
        if (idProdiFeeder == null || idProdiFeeder.isEmpty()) {
            FeederResponse<String> response = new FeederResponse<>();
            response.setErrorCode(-1);
            response.setErrorDesc("ID Prodi tidak boleh kosong");
            return response;
        }

        if (idSemesterFeeder == null || idSemesterFeeder.isEmpty()) {
            FeederResponse<String> response = new FeederResponse<>();
            response.setErrorCode(-1);
            response.setErrorDesc("ID Semester tidak boleh kosong");
            return response;
        }

        if (idMatkulFeeder == null || idMatkulFeeder.isEmpty()) {
            FeederResponse<String> response = new FeederResponse<>();
            response.setErrorCode(-1);
            response.setErrorDesc("ID Matakuliah tidak boleh kosong");
            return response;
        }

        if (namaKelas == null || namaKelas.isEmpty()) {
            FeederResponse<String> response = new FeederResponse<>();
            response.setErrorCode(-1);
            response.setErrorDesc("Nama Kelas tidak boleh kosong");
            return response;
        }

        Map<String, Object> record = new java.util.LinkedHashMap<>();
        record.put("id_prodi", idProdiFeeder);
        record.put("id_semester", idSemesterFeeder);
        record.put("id_matkul", idMatkulFeeder);
        record.put("nama_kelas_kuliah", namaKelas);
        record.put("lingkup", lingkup != null ? lingkup : 1);
        record.put("mode", mode != null ? mode : "O");
        if (kapasitas != null && kapasitas > 0) {
            record.put("kapasitas", kapasitas);
        }

        // Set other optional fields to null as requested
        record.put("bahasan", null);
        record.put("tanggal_mulai_efektif", null);
        record.put("tanggal_akhir_efektif", null);
        record.put("tanggal_tutup_daftar", null);
        record.put("apa_untuk_pditt", 0);

        log.debug("Feeder insert record: {}", record);

        FeederResponse<Object> response = executeWithRecord("InsertKelasKuliah", record);

        log.debug("Feeder insert response - success: {}, errorCode: {}, errorDesc: {}, data: {}",
                response.isSuccess(), response.getErrorCode(), response.getErrorDesc(), response.getData());

        FeederResponse<String> result = new FeederResponse<>();
        result.setErrorCode(response.getErrorCode());
        result.setErrorDesc(response.getErrorDesc());

        if (response.isSuccess() && response.getData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getData();
            String idKelasKuliah = (String) data.get("id_kelas_kuliah");
            result.setData(idKelasKuliah);
            log.info("Kelas kuliah inserted successfully with ID: {}", idKelasKuliah);
        } else {
            log.error("Failed to insert kelas kuliah: {}", response.getErrorDesc());
        }

        return result;
    }

    private FeederResponse<Object> createErrorResponse(int errorCode, String errorDesc) {
        FeederResponse<Object> response = new FeederResponse<>();
        response.setErrorCode(errorCode);
        response.setErrorDesc(errorDesc);
        return response;
    }

    // =====================================================
    // Dosen Pengajar Kelas Kuliah Methods
    // =====================================================

    /**
     * Get semua penugasan dosen dari Feeder berdasarkan list NIDN (BATCH dengan OR
     * filter)
     * Ini menghindari N+1 problem saat prepare insert dosen pengajar
     * 
     * @param nidns List NIDN dosen yang akan dicari
     * @return Map dari NIDN ke id_registrasi_dosen
     */
    public Map<String, String> getAllPenugasanDosenByNidns(List<String> nidns) {
        Map<String, String> result = new HashMap<>();

        if (nidns == null || nidns.isEmpty()) {
            return result;
        }

        // Build filter dengan OR untuk semua NIDN
        // Contoh: nidn='123' OR nidn='456' OR nidn='789'
        String filter = nidns.stream()
                .map(nidn -> String.format("nidn='%s'", nidn.trim()))
                .collect(java.util.stream.Collectors.joining(" OR "));

        log.info("Fetching penugasan dosen from Feeder with filter for {} NIDNs", nidns.size());
        log.debug("Filter: {}", filter);

        FeederResponse<Object> response = execute("GetListPenugasanDosen", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            log.info("Found {} penugasan dosen matching filter", dataList.size());

            // Build lookup map dari NIDN ke id_registrasi_dosen
            for (Object item : dataList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dosen = (Map<String, Object>) item;
                String nidn = (String) dosen.get("nidn");
                String idRegistrasiDosen = (String) dosen.get("id_registrasi_dosen");

                if (nidn != null && idRegistrasiDosen != null) {
                    result.put(nidn.trim(), idRegistrasiDosen);
                }
            }
        }

        log.info("Matched {} NIDNs from {} requested", result.size(), nidns.size());
        return result;
    }

    /**
     * Get semua existing dosen pengajar untuk list kelas kuliah (BATCH dengan OR
     * filter)
     * 
     * @param kelasKuliahIds List ID kelas kuliah Feeder
     * @return Map dari id_kelas_kuliah ke list id_registrasi_dosen yang sudah ada
     */
    public Map<String, List<String>> getExistingDosenPengajarByKelas(List<String> kelasKuliahIds) {
        Map<String, List<String>> result = new HashMap<>();

        if (kelasKuliahIds == null || kelasKuliahIds.isEmpty()) {
            return result;
        }

        // Build filter dengan OR untuk semua kelas kuliah
        // Contoh: id_kelas_kuliah='xxx' OR id_kelas_kuliah='yyy'
        String filter = kelasKuliahIds.stream()
                .map(id -> String.format("id_kelas_kuliah='%s'", id))
                .collect(java.util.stream.Collectors.joining(" OR "));

        log.info("Fetching existing dosen pengajar from Feeder with filter for {} kelas", kelasKuliahIds.size());
        log.debug("Filter: {}", filter);

        FeederResponse<Object> response = execute("GetListDosenPengajarKelasKuliah", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            log.info("Found {} existing dosen pengajar matching filter", dataList.size());

            for (Object item : dataList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dp = (Map<String, Object>) item;
                String idKelas = (String) dp.get("id_kelas_kuliah");
                String idDosen = (String) dp.get("id_registrasi_dosen");

                if (idKelas != null && idDosen != null) {
                    result.computeIfAbsent(idKelas, k -> new ArrayList<>()).add(idDosen);
                }
            }
        }

        log.info("Found existing dosen pengajar for {} kelas", result.size());
        return result;
    }

    /**
     * Get id_registrasi_dosen dari Feeder berdasarkan NIDN
     * Menggunakan GetListPenugasanDosen karena id_registrasi_dosen ada di sana
     * 
     * @param nidn NIDN dosen
     * @return id_registrasi_dosen atau null jika tidak ditemukan
     */
    @Cacheable(value = "feederIdRegistrasiDosen", key = "#nidn", unless = "#result == null")
    public String getIdRegistrasiDosen(String nidn) {
        if (nidn == null || nidn.trim().isEmpty()) {
            log.warn("NIDN kosong, tidak bisa mencari id_registrasi_dosen");
            return null;
        }

        String filter = String.format("nidn='%s'", nidn.trim());
        log.debug("Searching dosen in Feeder (GetListPenugasanDosen) with filter: {}", filter);

        // Gunakan GetListPenugasanDosen karena id_registrasi_dosen ada di sana
        // GetListDosen tidak memiliki id_registrasi_dosen
        FeederResponse<Object> response = execute("GetListPenugasanDosen", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            if (!dataList.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> first = (Map<String, Object>) dataList.get(0);
                String idRegistrasiDosen = (String) first.get("id_registrasi_dosen");
                log.info("Found id_registrasi_dosen: {} for NIDN: {}", idRegistrasiDosen, nidn);
                return idRegistrasiDosen;
            }
        }

        log.warn("Dosen not found in Feeder (GetListPenugasanDosen) for NIDN: {}", nidn);
        return null;
    }

    /**
     * Insert dosen pengajar kelas kuliah ke Feeder
     * 
     * @param idRegistrasiDosen  ID registrasi dosen dari Feeder
     * @param idKelasKuliah      ID kelas kuliah dari Feeder
     * @param sksSubstansiTotal  Total SKS substansi
     * @param rencanaTatapMuka   Rencana jumlah tatap muka
     * @param realisasiTatapMuka Realisasi jumlah tatap muka
     * @param idJenisEvaluasi    Jenis evaluasi (1=default)
     * @return FeederResponse dengan id_aktivitas_mengajar jika sukses
     */
    public FeederResponse<String> insertDosenPengajarKelasKuliah(
            String idRegistrasiDosen,
            String idKelasKuliah,
            Integer sksSubstansiTotal,
            Integer rencanaTatapMuka,
            Integer realisasiTatapMuka,
            Integer idJenisEvaluasi) {

        log.info("Inserting dosen pengajar to Feeder - idDosen: {}, idKelas: {}, sks: {}",
                idRegistrasiDosen, idKelasKuliah, sksSubstansiTotal);

        // Validasi required fields
        if (idRegistrasiDosen == null || idRegistrasiDosen.isEmpty()) {
            FeederResponse<String> response = new FeederResponse<>();
            response.setErrorCode(-1);
            response.setErrorDesc("ID Registrasi Dosen tidak boleh kosong");
            return response;
        }

        if (idKelasKuliah == null || idKelasKuliah.isEmpty()) {
            FeederResponse<String> response = new FeederResponse<>();
            response.setErrorCode(-1);
            response.setErrorDesc("ID Kelas Kuliah tidak boleh kosong");
            return response;
        }

        Map<String, Object> record = new java.util.LinkedHashMap<>();
        record.put("id_registrasi_dosen", idRegistrasiDosen);
        record.put("id_kelas_kuliah", idKelasKuliah);
        record.put("id_substansi", null);
        record.put("sks_substansi_total", sksSubstansiTotal != null ? sksSubstansiTotal.toString() : "0");
        record.put("rencana_tatap_muka", rencanaTatapMuka != null ? rencanaTatapMuka.toString() : "14");
        record.put("realisasi_tatap_muka", realisasiTatapMuka != null ? realisasiTatapMuka.toString() : "14");
        record.put("rencana_minggu_pertemuan", "16"); // Default 16 pertemuan per semester
        record.put("id_jenis_evaluasi", idJenisEvaluasi != null ? idJenisEvaluasi : 1);

        log.debug("Feeder InsertDosenPengajarKelasKuliah record: {}", record);

        FeederResponse<Object> response = executeWithRecord("InsertDosenPengajarKelasKuliah", record);

        FeederResponse<String> result = new FeederResponse<>();
        result.setErrorCode(response.getErrorCode());
        result.setErrorDesc(response.getErrorDesc());

        if (response.isSuccess() && response.getData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getData();
            String idAktivitasMengajar = (String) data.get("id_aktivitas_mengajar");
            result.setData(idAktivitasMengajar);
            log.info("Dosen pengajar inserted successfully with ID: {}", idAktivitasMengajar);
        } else {
            log.error("Failed to insert dosen pengajar: {}", response.getErrorDesc());
        }

        return result;
    }

    /**
     * Check apakah dosen pengajar sudah ada di Feeder untuk kelas tertentu
     * 
     * @param idKelasKuliah     ID kelas kuliah dari Feeder
     * @param idRegistrasiDosen ID registrasi dosen dari Feeder
     * @return true jika sudah ada, false jika belum
     */
    public boolean isDosenPengajarExists(String idKelasKuliah, String idRegistrasiDosen) {
        String filter = String.format("id_kelas_kuliah='%s' AND id_registrasi_dosen='%s'",
                idKelasKuliah, idRegistrasiDosen);

        FeederResponse<Object> response = execute("GetListDosenPengajarKelasKuliah", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            return !dataList.isEmpty();
        }

        return false;
    }

    // =====================================================
    // Peserta Kelas Kuliah Methods
    // =====================================================

    /**
     * Get semua id_registrasi_mahasiswa dari Feeder secara BATCH berdasarkan list
     * NIM
     * Optimized: Satu API call untuk banyak mahasiswa menggunakan OR filter
     * 
     * @param nims List NIM mahasiswa yang akan dicari
     * @return Map dari NIM ke id_registrasi_mahasiswa
     */
    public Map<String, String> getAllMahasiswaByNims(List<String> nims) {
        Map<String, String> result = new HashMap<>();

        if (nims == null || nims.isEmpty()) {
            return result;
        }

        // Build filter dengan OR untuk semua NIM
        // Contoh: nim='123456' OR nim='789012' OR nim='345678'
        String filter = nims.stream()
                .map(nim -> String.format("nim='%s'", nim.trim()))
                .collect(java.util.stream.Collectors.joining(" OR "));

        log.info("Fetching mahasiswa from Feeder with filter for {} NIMs", nims.size());
        log.debug("Filter: {}", filter);

        FeederResponse<Object> response = execute("GetListRiwayatPendidikanMahasiswa", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            log.info("Found {} mahasiswa matching filter", dataList.size());

            // Build lookup map dari NIM ke id_registrasi_mahasiswa
            for (Object item : dataList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mhs = (Map<String, Object>) item;
                String nim = (String) mhs.get("nim");
                String idRegistrasiMahasiswa = (String) mhs.get("id_registrasi_mahasiswa");

                if (nim != null && idRegistrasiMahasiswa != null) {
                    result.put(nim.trim(), idRegistrasiMahasiswa);
                }
            }
        }

        log.info("Matched {} NIMs from {} requested", result.size(), nims.size());
        return result;
    }

    /**
     * Get semua existing peserta kelas untuk list kelas kuliah (BATCH dengan OR
     * filter)
     * 
     * @param kelasKuliahIds List ID kelas kuliah Feeder
     * @return Map dari id_kelas_kuliah ke list id_registrasi_mahasiswa yang sudah
     *         ada
     */
    public Map<String, List<String>> getExistingPesertaKelasByKelas(List<String> kelasKuliahIds) {
        Map<String, List<String>> result = new HashMap<>();

        if (kelasKuliahIds == null || kelasKuliahIds.isEmpty()) {
            return result;
        }

        // Build filter dengan OR untuk semua kelas kuliah
        String filter = kelasKuliahIds.stream()
                .map(id -> String.format("id_kelas_kuliah='%s'", id))
                .collect(java.util.stream.Collectors.joining(" OR "));

        log.info("Fetching existing peserta kelas from Feeder with filter for {} kelas", kelasKuliahIds.size());
        log.debug("Filter: {}", filter);

        FeederResponse<Object> response = execute("GetPesertaKelasKuliah", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            log.info("Found {} existing peserta kelas matching filter", dataList.size());

            for (Object item : dataList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> peserta = (Map<String, Object>) item;
                String idKelas = (String) peserta.get("id_kelas_kuliah");
                String idMahasiswa = (String) peserta.get("id_registrasi_mahasiswa");

                if (idKelas != null && idMahasiswa != null) {
                    result.computeIfAbsent(idKelas, k -> new ArrayList<>()).add(idMahasiswa);
                }
            }
        }

        log.info("Found existing peserta for {} kelas", result.size());
        return result;
    }

    /**
     * Get id_registrasi_mahasiswa dari Feeder berdasarkan NIM
     * 
     * @param nim NIM mahasiswa
     * @return id_registrasi_mahasiswa atau null jika tidak ditemukan
     */
    public String getIdRegistrasiMahasiswa(String nim) {
        if (nim == null || nim.trim().isEmpty()) {
            log.warn("NIM kosong, tidak bisa mencari id_registrasi_mahasiswa");
            return null;
        }

        String filter = String.format("nim='%s'", nim.trim());
        log.debug("Searching mahasiswa in Feeder with filter: {}", filter);

        FeederResponse<Object> response = execute("GetListRiwayatPendidikanMahasiswa", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            if (!dataList.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> first = (Map<String, Object>) dataList.get(0);
                String idRegistrasiMahasiswa = (String) first.get("id_registrasi_mahasiswa");
                log.info("Found id_registrasi_mahasiswa: {} for NIM: {}", idRegistrasiMahasiswa, nim);
                return idRegistrasiMahasiswa;
            }
        }

        log.warn("Mahasiswa not found in Feeder for NIM: {}", nim);
        return null;
    }

    /**
     * Insert peserta kelas kuliah ke Feeder
     * 
     * @param idKelasKuliah         ID kelas kuliah dari Feeder
     * @param idRegistrasiMahasiswa ID registrasi mahasiswa dari Feeder
     * @return FeederResponse dengan id_kelas_kuliah jika sukses
     */
    public FeederResponse<String> insertPesertaKelasKuliah(
            String idKelasKuliah,
            String idRegistrasiMahasiswa) {

        log.info("Inserting peserta kelas to Feeder - idKelas: {}, idMhs: {}",
                idKelasKuliah, idRegistrasiMahasiswa);

        // Validasi required fields
        if (idKelasKuliah == null || idKelasKuliah.isEmpty()) {
            FeederResponse<String> response = new FeederResponse<>();
            response.setErrorCode(-1);
            response.setErrorDesc("ID Kelas Kuliah tidak boleh kosong");
            return response;
        }

        if (idRegistrasiMahasiswa == null || idRegistrasiMahasiswa.isEmpty()) {
            FeederResponse<String> response = new FeederResponse<>();
            response.setErrorCode(-1);
            response.setErrorDesc("ID Registrasi Mahasiswa tidak boleh kosong");
            return response;
        }

        Map<String, Object> record = new java.util.LinkedHashMap<>();
        record.put("id_kelas_kuliah", idKelasKuliah);
        record.put("id_registrasi_mahasiswa", idRegistrasiMahasiswa);
        record.put("rencana_minggu_pertemuan", "16"); // Default 16 pertemuan per semester

        log.debug("Feeder InsertPesertaKelasKuliah record: {}", record);

        FeederResponse<Object> response = executeWithRecord("InsertPesertaKelasKuliah", record);

        FeederResponse<String> result = new FeederResponse<>();
        result.setErrorCode(response.getErrorCode());
        result.setErrorDesc(response.getErrorDesc());

        if (response.isSuccess() && response.getData() != null) {
            // InsertPesertaKelasKuliah biasanya mengembalikan id_kelas_kuliah
            if (response.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getData();
                String id = data.get("id_kelas_kuliah") != null
                        ? (String) data.get("id_kelas_kuliah")
                        : idKelasKuliah;
                result.setData(id);
            } else {
                result.setData(idKelasKuliah);
            }
            log.info("Peserta kelas inserted successfully");
        } else {
            log.error("Failed to insert peserta kelas: {}", response.getErrorDesc());
        }

        return result;
    }

    /**
     * Check apakah peserta sudah terdaftar di kelas di Feeder
     * 
     * @param idKelasKuliah         ID kelas kuliah dari Feeder
     * @param idRegistrasiMahasiswa ID registrasi mahasiswa dari Feeder
     * @return true jika sudah ada, false jika belum
     */
    public boolean isPesertaKelasExists(String idKelasKuliah, String idRegistrasiMahasiswa) {
        String filter = String.format("id_kelas_kuliah='%s' AND id_registrasi_mahasiswa='%s'",
                idKelasKuliah, idRegistrasiMahasiswa);

        FeederResponse<Object> response = execute("GetListPesertaKelasKuliah", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            return !dataList.isEmpty();
        }

        return false;
    }

    // =====================================================
    // Nilai Perkuliahan Methods
    // =====================================================

    /**
     * Update nilai perkuliahan kelas ke Feeder
     * 
     * @param idRegistrasiMahasiswa ID registrasi mahasiswa dari Feeder
     * @param idKelasKuliah         ID kelas kuliah dari Feeder
     * @param nilaiAngka            Nilai angka (0-100)
     * @param nilaiIndeks           Nilai indeks/bobot (0-4)
     * @param nilaiHuruf            Nilai huruf (A, B, C, D, E)
     * @return FeederResponse
     */
    public FeederResponse<Object> updateNilaiPerkuliahanKelas(
            String idRegistrasiMahasiswa,
            String idKelasKuliah,
            Double nilaiAngka,
            Double nilaiIndeks,
            String nilaiHuruf) {

        log.info("Updating nilai perkuliahan to Feeder - idMhs: {}, idKelas: {}, nilai: {} ({})",
                idRegistrasiMahasiswa, idKelasKuliah, nilaiAngka, nilaiHuruf);

        // Validasi required fields
        if (idRegistrasiMahasiswa == null || idRegistrasiMahasiswa.isEmpty()) {
            return createErrorResponse(-1, "ID Registrasi Mahasiswa tidak boleh kosong");
        }

        if (idKelasKuliah == null || idKelasKuliah.isEmpty()) {
            return createErrorResponse(-1, "ID Kelas Kuliah tidak boleh kosong");
        }

        if (nilaiHuruf == null || nilaiHuruf.trim().isEmpty()) {
            return createErrorResponse(-1, "Nilai Huruf tidak boleh kosong");
        }

        // Build key untuk update
        Map<String, Object> key = new java.util.LinkedHashMap<>();
        key.put("id_registrasi_mahasiswa", idRegistrasiMahasiswa);
        key.put("id_kelas_kuliah", idKelasKuliah);

        // Build record dengan nilai
        Map<String, Object> record = new java.util.LinkedHashMap<>();
        record.put("nilai_angka", nilaiAngka != null ? nilaiAngka : 0);
        record.put("nilai_indeks", nilaiIndeks != null ? nilaiIndeks : 0);
        record.put("nilai_huruf", nilaiHuruf.trim());

        log.debug("Feeder UpdateNilaiPerkuliahanKelas key: {}, record: {}", key, record);

        FeederResponse<Object> response = update("UpdateNilaiPerkuliahanKelas", key, record);

        if (response.isSuccess()) {
            log.info("Nilai perkuliahan updated successfully - idMhs: {}, idKelas: {}", 
                    idRegistrasiMahasiswa, idKelasKuliah);
        } else {
            log.error("Failed to update nilai perkuliahan: {}", response.getErrorDesc());
        }

        return response;
    }

    /**
     * Get detail nilai perkuliahan kelas dari Feeder
     * 
     * @param idKelasKuliah ID kelas kuliah dari Feeder
     * @return List of nilai data
     */
    public List<Map<String, Object>> getDetailNilaiPerkuliahanKelas(String idKelasKuliah) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (idKelasKuliah == null || idKelasKuliah.isEmpty()) {
            return result;
        }

        String filter = String.format("id_kelas_kuliah='%s'", idKelasKuliah);
        log.debug("Fetching nilai perkuliahan from Feeder with filter: {}", filter);

        FeederResponse<Object> response = execute("GetDetailNilaiPerkuliahanKelas", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            for (Object item : dataList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nilai = (Map<String, Object>) item;
                result.add(nilai);
            }
        }

        return result;
    }

    /**
     * Get detail nilai perkuliahan untuk multiple kelas (BATCH)
     * 
     * @param kelasKuliahIds List ID kelas kuliah Feeder
     * @return Map dari id_kelas_kuliah ke Map<id_registrasi_mahasiswa, nilai_data>
     */
    public Map<String, Map<String, Map<String, Object>>> getDetailNilaiPerkuliahanKelasBatch(
            List<String> kelasKuliahIds) {
        
        Map<String, Map<String, Map<String, Object>>> result = new HashMap<>();

        if (kelasKuliahIds == null || kelasKuliahIds.isEmpty()) {
            return result;
        }

        // Build filter dengan OR untuk semua kelas kuliah
        String filter = kelasKuliahIds.stream()
                .map(id -> String.format("id_kelas_kuliah='%s'", id))
                .collect(java.util.stream.Collectors.joining(" OR "));

        log.info("Batch fetching nilai perkuliahan from Feeder for {} kelas", kelasKuliahIds.size());
        log.debug("Filter: {}", filter);

        FeederResponse<Object> response = execute("GetDetailNilaiPerkuliahanKelas", filter);

        if (response.isSuccess() && response.getData() != null) {
            List<?> dataList = (List<?>) response.getData();
            log.info("Found {} nilai records from Feeder", dataList.size());

            for (Object item : dataList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nilai = (Map<String, Object>) item;
                String idKelas = (String) nilai.get("id_kelas_kuliah");
                String idMahasiswa = (String) nilai.get("id_registrasi_mahasiswa");

                if (idKelas != null && idMahasiswa != null) {
                    result.computeIfAbsent(idKelas, k -> new HashMap<>())
                          .put(idMahasiswa, nilai);
                }
            }
        }

        log.info("Found nilai data for {} kelas", result.size());
        return result;
    }
}
