package id.ac.tazkia.smilemahasiswa.feeder.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import id.ac.tazkia.smilemahasiswa.feeder.service.FeederAuthService;
import id.ac.tazkia.smilemahasiswa.feeder.service.FeederService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller untuk health check dan token management Feeder API.
 * 
 * Endpoints:
 * - GET /api/feeder/health - Health check
 * - GET /api/feeder/test/token - Test token acquisition
 * - POST /api/feeder/token/refresh - Force refresh token
 * - DELETE /api/feeder/token - Clear token cache
 */
@Slf4j
@RestController
@RequestMapping("/api/feeder")
@RequiredArgsConstructor
public class FeederHealthController {

    private final FeederService feederService;
    private final FeederAuthService authService;

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
    // Test Endpoints
    // =====================================================

    /**
     * Test get data prodi
     * GET /api/feeder/test/prodi
     */
    @GetMapping("/test/prodi")
    public FeederResponse<Object> testGetProdi() {
        log.info("Testing GetProdi...");
        return feederService.execute("GetProdi");
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
     * Test get list matakuliah
     * GET /api/feeder/test/listmatakul?limit=10
     */
    @GetMapping("/test/listmatakul")
    public FeederResponse<Object> testGetListMatakul(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("Testing GetListMatakul with limit {}...", limit);
        return feederService.execute("GetListMataKuliah", null, null, limit, 0);
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
}
