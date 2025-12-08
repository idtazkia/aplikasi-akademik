package id.ac.tazkia.smilemahasiswa.api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.tazkia.smilemahasiswa.dao.ApiClientDao;
import id.ac.tazkia.smilemahasiswa.dao.UserDao;
import id.ac.tazkia.smilemahasiswa.entity.ApiClient;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.security.JwtUtil;
import lombok.Data;

/**
 * Admin Controller for API Client Management
 * Hanya bisa diakses oleh SUPERUSER
 */
@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAdminController.class);

    @Autowired
    private ApiClientDao apiClientDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Helper endpoint untuk admin mendapatkan JWT token
     * Digunakan untuk bootstrap pertama kali atau testing
     * 
     * GET /api/admin/admin-token
     * 
     * Admin harus sudah login ke web aplikasi SMILE (OAuth2 session)
     */
    @GetMapping(value = "/admin-token", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('VIEW_SUPERUSER')")
    public ResponseEntity<?> getAdminToken(Authentication authentication) {

        // Extract email dari OAuth2 user
        String email = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser) {
            org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser oidcUser = (org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser) principal;
            email = oidcUser.getEmail();
        } else if (principal instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User) {
            org.springframework.security.oauth2.core.user.DefaultOAuth2User oauth2User = (org.springframework.security.oauth2.core.user.DefaultOAuth2User) principal;
            email = (String) oauth2User.getAttributes().get("email");
        }

        if (email == null) {
            LOGGER.error("Email not found in OAuth2 principal");
            return ResponseEntity.status(401)
                    .body(createErrorResponse("Email not found in OAuth2 authentication"));
        }

        // Query User dari database
        User admin = userDao.findByUsername(email);
        if (admin == null) {
            LOGGER.error("User not found in database: {}", email);
            return ResponseEntity.status(401)
                    .body(createErrorResponse("User not found in database: " + email));
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(admin.getUsername());

        LOGGER.info("Admin token generated for: {}", admin.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("tokenType", "Bearer");
        response.put("username", admin.getUsername());
        response.put("expiresIn", 86400000); // 24 jam
        response.put("note", "Use this token in Authorization header for admin API calls");

        return ResponseEntity.ok(response);
    }

    /**
     * Generate API key untuk aplikasi baru
     * 
     * POST /api/admin/generate-api-key
     * Body: { "clientName": "SPMB System", "notes": "Untuk integrasi SPMB",
     * "rateLimitPerHour": 1000 }
     */
    @PostMapping(value = "/generate-api-key", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('VIEW_SUPERUSER')")
    public ResponseEntity<?> generateApiKey(@RequestBody GenerateKeyRequest request,
            Authentication authentication) {

        LOGGER.info("Generate API key request for client: {}", request.getClientName());

        // Validate input
        if (request.getClientName() == null || request.getClientName().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Client name is required"));
        }

        // Check if client name already exists
        ApiClient existing = apiClientDao.findByClientNameAndIsActiveTrue(request.getClientName());
        if (existing != null) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Client name already exists"));
        }

        // Get current user
        User currentUser = (User) authentication.getPrincipal();

        // Generate random API key
        String apiKey = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

        // Hash API key dengan BCrypt
        String hashedKey = BCrypt.hashpw(apiKey, BCrypt.gensalt(12));

        // Create API client
        ApiClient client = new ApiClient();
        client.setClientName(request.getClientName());
        client.setApiKeyHash(hashedKey);
        client.setCreatedBy(currentUser);
        client.setCreatedAt(LocalDateTime.now());
        client.setIsActive(true);
        client.setRateLimitPerHour(request.getRateLimitPerHour() != null ? request.getRateLimitPerHour() : 1000);
        client.setAllowedIps(request.getAllowedIps());
        client.setNotes(request.getNotes());

        apiClientDao.save(client);

        LOGGER.info("API key generated for client: {} by user: {}",
                request.getClientName(), currentUser.getUsername());

        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("clientId", client.getId());
        response.put("clientName", client.getClientName());
        response.put("apiKey", apiKey);
        response.put("createdAt", client.getCreatedAt().toString());
        response.put("rateLimitPerHour", client.getRateLimitPerHour());
        response.put("warning", "⚠️ SAVE THIS API KEY! It will NOT be shown again. Store it securely.");

        return ResponseEntity.ok(response);
    }

    /**
     * List semua API clients
     * 
     * GET /api/admin/clients
     */
    @GetMapping(value = "/clients", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('VIEW_SUPERUSER')")
    public ResponseEntity<?> listClients() {

        Iterable<ApiClient> clients = apiClientDao.findAll();

        List<Map<String, Object>> clientList = StreamSupport.stream(clients.spliterator(), false)
                .map(client -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", client.getId());
                    map.put("clientName", client.getClientName());
                    map.put("isActive", client.getIsActive());
                    map.put("createdAt", client.getCreatedAt() != null ? client.getCreatedAt().toString() : null);
                    map.put("lastUsedAt", client.getLastUsedAt() != null ? client.getLastUsedAt().toString() : null);
                    map.put("lastUsedBy", client.getLastUsedBy());
                    map.put("rateLimitPerHour", client.getRateLimitPerHour());
                    map.put("allowedIps", client.getAllowedIps());
                    map.put("notes", client.getNotes());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(clientList);
    }

    /**
     * Deactivate API client
     * 
     * POST /api/admin/deactivate-client/{clientId}
     */
    @PostMapping(value = "/deactivate-client/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('VIEW_SUPERUSER')")
    public ResponseEntity<?> deactivateClient(@PathVariable String clientId, Authentication authentication) {

        ApiClient client = apiClientDao.findById(clientId).orElse(null);

        if (client == null) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = (User) authentication.getPrincipal();

        client.setIsActive(false);
        apiClientDao.save(client);

        LOGGER.info("API client deactivated: {} by user: {}", client.getClientName(), currentUser.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Client deactivated successfully");
        response.put("clientName", client.getClientName());

        return ResponseEntity.ok(response);
    }

    /**
     * Activate API client
     * 
     * POST /api/admin/activate-client/{clientId}
     */
    @PostMapping(value = "/activate-client/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('VIEW_SUPERUSER')")
    public ResponseEntity<?> activateClient(@PathVariable String clientId, Authentication authentication) {

        ApiClient client = apiClientDao.findById(clientId).orElse(null);

        if (client == null) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = (User) authentication.getPrincipal();

        client.setIsActive(true);
        apiClientDao.save(client);

        LOGGER.info("API client activated: {} by user: {}", client.getClientName(), currentUser.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Client activated successfully");
        response.put("clientName", client.getClientName());

        return ResponseEntity.ok(response);
    }

    /**
     * Regenerate API key untuk client yang sudah ada
     * 
     * POST /api/admin/regenerate-api-key/{clientId}
     */
    @PostMapping(value = "/regenerate-api-key/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('VIEW_SUPERUSER')")
    public ResponseEntity<?> regenerateApiKey(@PathVariable String clientId, Authentication authentication) {

        ApiClient client = apiClientDao.findById(clientId).orElse(null);

        if (client == null) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = (User) authentication.getPrincipal();

        // Generate new API key
        String newApiKey = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String hashedKey = BCrypt.hashpw(newApiKey, BCrypt.gensalt(12));

        client.setApiKeyHash(hashedKey);
        client.setCreatedAt(LocalDateTime.now());
        apiClientDao.save(client);

        LOGGER.info("API key regenerated for client: {} by user: {}",
                client.getClientName(), currentUser.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("clientName", client.getClientName());
        response.put("apiKey", newApiKey);
        response.put("regeneratedAt", client.getCreatedAt().toString());
        response.put("warning", "⚠️ SAVE THIS NEW API KEY! Old key is now invalid.");

        return ResponseEntity.ok(response);
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    @Data
    public static class GenerateKeyRequest {
        private String clientName;
        private String notes;
        private Integer rateLimitPerHour;
        private String allowedIps; // Comma separated IPs
    }
}
