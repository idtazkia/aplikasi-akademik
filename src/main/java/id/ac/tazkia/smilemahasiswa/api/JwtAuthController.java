package id.ac.tazkia.smilemahasiswa.api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.tazkia.smilemahasiswa.dao.ApiAccessLogDao;
import id.ac.tazkia.smilemahasiswa.dao.ApiClientDao;
import id.ac.tazkia.smilemahasiswa.dao.UserDao;
import id.ac.tazkia.smilemahasiswa.entity.ApiAccessLog;
import id.ac.tazkia.smilemahasiswa.entity.ApiClient;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.security.JwtUtil;
import lombok.Data;

/**
 * JWT Authentication Controller
 * Provides endpoint to generate JWT tokens for API authentication
 */
@RestController
@RequestMapping("/api/auth")
public class JwtAuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthController.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ApiClientDao apiClientDao;

    @Autowired
    private ApiAccessLogDao apiAccessLogDao;

    /**
     * Generate JWT token
     * 
     * POST /api/auth/token
     * Body: { "username": "user@example.com", "apiKey": "your-api-key" }
     * 
     * Response: { "token": "jwt-token-here", "expiresIn": 86400000 }
     */
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generateToken(@RequestBody TokenRequest request, HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        LOGGER.info("Token generation request for username: {} from IP: {}", request.getUsername(), ipAddress);

        // Validate input
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Username is required"));
        }

        if (request.getApiKey() == null || request.getApiKey().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("API key is required"));
        }

        // Verify user exists
        User user = userDao.findByUsername(request.getUsername());
        if (user == null) {
            LOGGER.warn("User not found: {}", request.getUsername());
            logAccessAttempt(null, request.getUsername(), ipAddress, false, "User not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid credentials"));
        }

        // Find API client by trying to match the provided key with stored hashes
        ApiClient validClient = findValidApiClient(request.getApiKey());

        if (validClient == null) {
            LOGGER.warn("Invalid API key for user: {}", request.getUsername());
            logAccessAttempt(null, request.getUsername(), ipAddress, false, "Invalid API key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid API key"));
        }

        // Check if client is active
        if (!validClient.getIsActive()) {
            LOGGER.warn("Inactive API client: {}", validClient.getClientName());
            logAccessAttempt(validClient, request.getUsername(), ipAddress, false, "Client inactive");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("API client is inactive"));
        }

        // Check IP whitelist (if configured)
        if (validClient.getAllowedIps() != null && !validClient.getAllowedIps().isEmpty()) {
            String[] allowedIps = validClient.getAllowedIps().split(",");
            boolean ipAllowed = false;
            for (String allowedIp : allowedIps) {
                if (allowedIp.trim().equals(ipAddress)) {
                    ipAllowed = true;
                    break;
                }
            }
            if (!ipAllowed) {
                LOGGER.warn("IP not allowed: {} for client: {}", ipAddress, validClient.getClientName());
                logAccessAttempt(validClient, request.getUsername(), ipAddress, false, "IP not allowed");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Access denied from this IP address"));
            }
        }

        // Check rate limit
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long requestsInLastHour = apiAccessLogDao.countByClientAndTimestampAfter(validClient, oneHourAgo);

        if (requestsInLastHour >= validClient.getRateLimitPerHour()) {
            LOGGER.warn("Rate limit exceeded for client: {}", validClient.getClientName());
            logAccessAttempt(validClient, request.getUsername(), ipAddress, false, "Rate limit exceeded");
            return ResponseEntity.status(429) // Too Many Requests
                    .body(createErrorResponse("Rate limit exceeded. Try again later."));
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        // Update last used info
        validClient.setLastUsedAt(LocalDateTime.now());
        validClient.setLastUsedBy(user.getUsername());
        apiClientDao.save(validClient);

        // Log successful access
        logAccessAttempt(validClient, request.getUsername(), ipAddress, true, null);

        LOGGER.info("JWT token generated successfully for user: {} via client: {}",
                request.getUsername(), validClient.getClientName());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 86400000); // 24 hours in milliseconds
        response.put("username", user.getUsername());
        response.put("clientName", validClient.getClientName());

        return ResponseEntity.ok(response);
    }

    /**
     * Find valid API client by matching provided key with stored hashes
     */
    private ApiClient findValidApiClient(String providedApiKey) {
        List<ApiClient> activeClients = apiClientDao.findByIsActiveTrue();

        for (ApiClient client : activeClients) {
            try {
                if (BCrypt.checkpw(providedApiKey, client.getApiKeyHash())) {
                    return client;
                }
            } catch (Exception e) {
                LOGGER.error("Error checking API key for client: {}", client.getClientName(), e);
            }
        }

        return null;
    }

    /**
     * Log access attempt (success or failure)
     */
    private void logAccessAttempt(ApiClient client, String username, String ipAddress,
            boolean success, String errorMessage) {
        try {
            ApiAccessLog log = new ApiAccessLog();
            log.setClient(client);
            log.setUsername(username);
            log.setIpAddress(ipAddress);
            log.setEndpoint("/api/auth/token");
            log.setTimestamp(LocalDateTime.now().plusHours(7));
            log.setSuccess(success);
            log.setErrorMessage(errorMessage);
            apiAccessLogDao.save(log);
        } catch (Exception e) {
            LOGGER.error("Failed to log access attempt", e);
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    @Data
    public static class TokenRequest {
        private String username;
        private String apiKey;
    }
}
