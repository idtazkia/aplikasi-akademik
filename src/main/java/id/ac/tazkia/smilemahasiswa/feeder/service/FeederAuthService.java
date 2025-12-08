package id.ac.tazkia.smilemahasiswa.feeder.service;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import id.ac.tazkia.smilemahasiswa.feeder.client.FeederApiClient;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederTokenData;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing PDDIKTI Feeder API authentication.
 * Handles token acquisition, caching, and automatic refresh.
 * 
 * Token lifecycle:
 * - Token expires 30 minutes after acquisition
 * - Token is refreshed automatically if it will expire within the buffer period
 * - Thread-safe token management using ReentrantLock
 */
@Slf4j
@Service
public class FeederAuthService {

    private final FeederApiClient feederApiClient;

    @Value("${feeder.username}")
    private String username;

    @Value("${feeder.password}")
    private String password;

    @Value("${feeder.token.expiry-buffer:300}")
    private int expiryBufferSeconds;

    // Token expiry time in minutes (Feeder tokens typically expire in 30 minutes)
    private static final int TOKEN_EXPIRY_MINUTES = 30;

    // Cached token data
    private volatile FeederTokenData cachedToken;

    // Lock for thread-safe token refresh
    private final ReentrantLock tokenLock = new ReentrantLock();

    public FeederAuthService(FeederApiClient feederApiClient) {
        this.feederApiClient = feederApiClient;
    }

    /**
     * Get a valid token, refreshing if necessary.
     * This method is thread-safe and will only perform one token refresh at a time.
     *
     * @return the current valid token, or null if unable to obtain one
     */
    public String getToken() {
        // Fast path: check if cached token is still valid
        if (isTokenValid()) {
            return cachedToken.getToken();
        }

        // Slow path: need to refresh token
        return refreshToken();
    }

    /**
     * Force refresh the token, regardless of its current state.
     *
     * @return the new token, or null if unable to obtain one
     */
    public String forceRefreshToken() {
        return refreshToken();
    }

    /**
     * Check if the current cached token is valid and won't expire soon.
     *
     * @return true if the token is valid and won't expire within the buffer period
     */
    public boolean isTokenValid() {
        return cachedToken != null &&
                cachedToken.isValid() &&
                !cachedToken.willExpireSoon(expiryBufferSeconds);
    }

    /**
     * Get the current token data (for debugging/monitoring purposes).
     *
     * @return the current cached token data, or null if not available
     */
    public FeederTokenData getTokenData() {
        return cachedToken;
    }

    /**
     * Clear the cached token (useful for testing or forced re-authentication).
     */
    public void clearToken() {
        tokenLock.lock();
        try {
            cachedToken = null;
            log.info("Feeder token cache cleared");
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * Refresh the token from the Feeder API.
     * Thread-safe: only one thread will perform the refresh at a time.
     *
     * @return the new token, or null if unable to obtain one
     */
    private String refreshToken() {
        tokenLock.lock();
        try {
            // Double-check after acquiring lock (another thread might have refreshed it)
            if (isTokenValid()) {
                log.debug("Token already refreshed by another thread");
                return cachedToken.getToken();
            }

            log.info("Refreshing Feeder API token...");

            FeederResponse<Map<String, Object>> response = feederApiClient.getToken(username, password);

            if (response.isSuccess() && response.getData() != null) {
                String token = extractTokenFromResponse(response.getData());

                if (token != null && !token.isEmpty()) {
                    cachedToken = FeederTokenData.create(token, TOKEN_EXPIRY_MINUTES);
                    log.info("Feeder token refreshed successfully. Expires at: {}", cachedToken.getExpiresAt());
                    return token;
                } else {
                    log.error("Token not found in Feeder response");
                    return null;
                }
            } else {
                log.error("Failed to get Feeder token: {} - {}",
                        response.getErrorCode(), response.getErrorDesc());
                return null;
            }
        } catch (Exception e) {
            log.error("Error refreshing Feeder token: {}", e.getMessage(), e);
            return null;
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * Extract the token string from the response data.
     * The token might be in different fields depending on the Feeder API version.
     */
    private String extractTokenFromResponse(Map<String, Object> data) {
        // Try common field names
        if (data.containsKey("token")) {
            return String.valueOf(data.get("token"));
        }
        if (data.containsKey("Token")) {
            return String.valueOf(data.get("Token"));
        }
        if (data.containsKey("access_token")) {
            return String.valueOf(data.get("access_token"));
        }

        // Log available keys for debugging
        log.warn("Token field not found in response. Available keys: {}", data.keySet());

        return null;
    }
}
