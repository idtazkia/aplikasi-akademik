package id.ac.tazkia.smilemahasiswa.feeder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response DTO for PDDIKTI Feeder API.
 * All Feeder API responses have the same structure:
 * - error_code: 0 for success, non-zero for error
 * - error_desc: description of the error (empty if success)
 * - data: the response data (structure varies by API function)
 *
 * @param <T> the type of data in the response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeederResponse<T> {

    @JsonProperty("error_code")
    private int errorCode;

    @JsonProperty("error_desc")
    private String errorDesc;

    private T data;

    /**
     * Check if the response indicates success (error_code == 0)
     */
    public boolean isSuccess() {
        return errorCode == 0;
    }

    /**
     * Check if the response indicates an error (error_code != 0)
     */
    public boolean isError() {
        return errorCode != 0;
    }

    /**
     * Check if the token has expired based on error description
     */
    public boolean isTokenExpired() {
        return errorDesc != null &&
                (errorDesc.toLowerCase().contains("token") &&
                        (errorDesc.toLowerCase().contains("expired") ||
                                errorDesc.toLowerCase().contains("invalid")));
    }
}
