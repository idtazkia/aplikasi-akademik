package id.ac.tazkia.smilemahasiswa.feeder.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to hold the Feeder API token and its metadata.
 * Used for caching the token and determining when it needs to be refreshed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeederTokenData {

    /**
     * The authentication token string
     */
    private String token;

    /**
     * When the token was obtained
     */
    private LocalDateTime obtainedAt;

    /**
     * When the token expires (typically 30 minutes after obtainedAt)
     */
    private LocalDateTime expiresAt;

    /**
     * Create a new token data with default 30 minutes expiry
     */
    public static FeederTokenData create(String token) {
        return create(token, 30);
    }

    /**
     * Create a new token data with specified expiry in minutes
     */
    public static FeederTokenData create(String token, int expiryMinutes) {
        LocalDateTime now = LocalDateTime.now();
        return new FeederTokenData(
                token,
                now,
                now.plusMinutes(expiryMinutes));
    }

    /**
     * Check if the token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if the token will expire within the given buffer seconds
     * This is useful to refresh the token before it actually expires
     */
    public boolean willExpireSoon(int bufferSeconds) {
        return LocalDateTime.now().plusSeconds(bufferSeconds).isAfter(expiresAt);
    }

    /**
     * Check if the token is valid (not expired)
     */
    public boolean isValid() {
        return token != null && !token.isEmpty() && !isExpired();
    }
}
