package id.ac.tazkia.smilemahasiswa.feeder.service;

import java.util.List;
import java.util.Map;

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

    private FeederResponse<Object> createErrorResponse(int errorCode, String errorDesc) {
        FeederResponse<Object> response = new FeederResponse<>();
        response.setErrorCode(errorCode);
        response.setErrorDesc(errorDesc);
        return response;
    }
}
