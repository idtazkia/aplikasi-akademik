package id.ac.tazkia.smilemahasiswa.feeder.client;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederRequest;
import id.ac.tazkia.smilemahasiswa.feeder.dto.FeederResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Low-level HTTP client for PDDIKTI Feeder API.
 * Handles all HTTP communication with the Feeder endpoint.
 * Uses WebClient for non-blocking HTTP calls.
 */
@Slf4j
@Component
public class FeederApiClient {

    @Value("${feeder.url}")
    private String feederUrl;

    private WebClient webClient;
    private final ObjectMapper objectMapper;

    public FeederApiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(feederUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
        log.info("FeederApiClient initialized with URL: {}", feederUrl);
    }

    /**
     * Send a request to the Feeder API (blocking)
     *
     * @param request the request to send
     * @return the raw response as a Map
     */
    public FeederResponse<Object> send(FeederRequest request) {
        try {
            log.debug("Sending Feeder request: act={}", request.getAct());

            String responseBody = webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Received Feeder response: {}",
                    responseBody != null && responseBody.length() > 200
                            ? responseBody.substring(0, 200) + "..."
                            : responseBody);

            return objectMapper.readValue(responseBody,
                    new TypeReference<FeederResponse<Object>>() {
                    });

        } catch (WebClientResponseException e) {
            log.error("HTTP error from Feeder API: {} - {}", e.getStatusCode(), e.getMessage());
            return createErrorResponse(-1, "HTTP Error: " + e.getStatusCode() + " - " + e.getMessage());
        } catch (Exception e) {
            log.error("Error calling Feeder API: {}", e.getMessage(), e);
            return createErrorResponse(-1, "Error: " + e.getMessage());
        }
    }

    /**
     * Send a request to the Feeder API (reactive/non-blocking)
     *
     * @param request the request to send
     * @return Mono containing the response
     */
    public Mono<FeederResponse<Object>> sendAsync(FeederRequest request) {
        log.debug("Sending async Feeder request: act={}", request.getAct());

        return webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        log.debug("Received async Feeder response");
                        return objectMapper.readValue(responseBody,
                                new TypeReference<FeederResponse<Object>>() {
                                });
                    } catch (Exception e) {
                        log.error("Error parsing Feeder response: {}", e.getMessage());
                        return createErrorResponse(-1, "Parse error: " + e.getMessage());
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("HTTP error from Feeder API: {} - {}", e.getStatusCode(), e.getMessage());
                    return Mono.just(createErrorResponse(-1,
                            "HTTP Error: " + e.getStatusCode() + " - " + e.getMessage()));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Error calling Feeder API: {}", e.getMessage());
                    return Mono.just(createErrorResponse(-1, "Error: " + e.getMessage()));
                });
    }

    /**
     * Send a request and parse the data as a specific type
     *
     * @param request  the request to send
     * @param dataType the class of the expected data type
     * @return the response with data parsed as the specified type
     */
    public <T> FeederResponse<T> send(FeederRequest request, Class<T> dataType) {
        FeederResponse<Object> rawResponse = send(request);
        return convertResponse(rawResponse, dataType);
    }

    /**
     * Send a request and parse the data as a list of specific type
     *
     * @param request     the request to send
     * @param elementType the class of the list element type
     * @return the response with data parsed as a list
     */
    public <T> FeederResponse<List<T>> sendForList(FeederRequest request, Class<T> elementType) {
        FeederResponse<Object> rawResponse = send(request);
        return convertResponseToList(rawResponse, elementType);
    }

    /**
     * Get GetToken request
     */
    public FeederResponse<Map<String, Object>> getToken(String username, String password) {
        FeederRequest request = FeederRequest.createGetTokenRequest(username, password);
        FeederResponse<Object> rawResponse = send(request);

        FeederResponse<Map<String, Object>> response = new FeederResponse<>();
        response.setErrorCode(rawResponse.getErrorCode());
        response.setErrorDesc(rawResponse.getErrorDesc());

        if (rawResponse.getData() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) rawResponse.getData();
            response.setData(dataMap);
        }

        return response;
    }

    @SuppressWarnings("unchecked")
    private <T> FeederResponse<T> convertResponse(FeederResponse<Object> rawResponse, Class<T> dataType) {
        FeederResponse<T> response = new FeederResponse<>();
        response.setErrorCode(rawResponse.getErrorCode());
        response.setErrorDesc(rawResponse.getErrorDesc());

        if (rawResponse.getData() != null) {
            try {
                T data = objectMapper.convertValue(rawResponse.getData(), dataType);
                response.setData(data);
            } catch (Exception e) {
                log.error("Error converting response data to {}: {}", dataType.getSimpleName(), e.getMessage());
            }
        }

        return response;
    }

    @SuppressWarnings("unchecked")
    private <T> FeederResponse<List<T>> convertResponseToList(FeederResponse<Object> rawResponse,
            Class<T> elementType) {
        FeederResponse<List<T>> response = new FeederResponse<>();
        response.setErrorCode(rawResponse.getErrorCode());
        response.setErrorDesc(rawResponse.getErrorDesc());

        if (rawResponse.getData() != null) {
            try {
                List<T> data = objectMapper.convertValue(rawResponse.getData(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
                response.setData(data);
            } catch (Exception e) {
                log.error("Error converting response data to List<{}>: {}", elementType.getSimpleName(),
                        e.getMessage());
            }
        }

        return response;
    }

    private FeederResponse<Object> createErrorResponse(int errorCode, String errorDesc) {
        FeederResponse<Object> response = new FeederResponse<>();
        response.setErrorCode(errorCode);
        response.setErrorDesc(errorDesc);
        return response;
    }
}
