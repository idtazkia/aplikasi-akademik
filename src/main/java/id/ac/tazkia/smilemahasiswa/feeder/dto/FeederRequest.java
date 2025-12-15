package id.ac.tazkia.smilemahasiswa.feeder.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic request DTO for PDDIKTI Feeder API.
 * All Feeder API calls use POST with JSON body containing:
 * - act: the function name (e.g., "GetToken", "GetMahasiswa", etc.)
 * - token: authentication token (not required for GetToken)
 * - filter: optional filter conditions
 * - order: optional ordering
 * - limit: optional limit
 * - offset: optional offset
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeederRequest {

    /**
     * The API function name (e.g., "GetToken", "GetMahasiswa", "InsertMahasiswa")
     */
    private String act;

    /**
     * Authentication token (required for all API calls except GetToken)
     */
    private String token;

    /**
     * Filter conditions for GET operations
     * Example: "id_prodi = '12345'"
     */
    private String filter;

    /**
     * Order clause for GET operations
     * Example: "nama_mahasiswa ASC"
     */
    private String order;

    /**
     * Limit number of records to return
     */
    private Integer limit;

    /**
     * Offset for pagination
     */
    private Integer offset;

    /**
     * Key-value record for Insert/Update operations
     * The structure depends on the specific API function
     */
    private Map<String, Object> record;

    /**
     * Key used for Update/Delete operations to identify the record
     */
    private Map<String, Object> key;

    /**
     * Username for GetToken request
     */
    private String username;

    /**
     * Password for GetToken request
     */
    private String password;

    /**
     * Create a GetToken request
     * The Feeder API expects username and password directly in the request body,
     * not inside a "record" field
     */
    public static FeederRequest createGetTokenRequest(String username, String password) {
        return FeederRequest.builder()
                .act("GetToken")
                .username(username)
                .password(password)
                .build();
    }

    /**
     * Create a simple GET request with token
     */
    public static FeederRequest createGetRequest(String act, String token) {
        return FeederRequest.builder()
                .act(act)
                .token(token)
                .build();
    }

    /**
     * Create a GET request with filter
     */
    public static FeederRequest createGetRequest(String act, String token, String filter) {
        return FeederRequest.builder()
                .act(act)
                .token(token)
                .filter(filter)
                .build();
    }

    /**
     * Create a GET request with pagination
     */
    public static FeederRequest createGetRequest(String act, String token, String filter,
            String order, Integer limit, Integer offset) {
        return FeederRequest.builder()
                .act(act)
                .token(token)
                .filter(filter)
                .order(order)
                .limit(limit)
                .offset(offset)
                .build();
    }

    /**
     * Create an Insert/Update request with record
     */
    public static FeederRequest createInsertRequest(String act, String token, Map<String, Object> record) {
        return FeederRequest.builder()
                .act(act)
                .token(token)
                .record(record)
                .build();
    }
}
