package com.crypto.cmtrade.cryptobot.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Data
@Component
public class RetryableRestTemplate {

    private RestTemplate restTemplate;
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY = 1000; // 1 second
    private static final double BACKOFF_MULTIPLIER = 2;

    public RetryableRestTemplate() {
        this.restTemplate = new RestTemplate();
    }

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables)
            throws RestClientException {
        return executeWithRetry(() -> restTemplate.exchange(url, method, requestEntity, responseType, uriVariables), url);
    }

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType)
            throws RestClientException {
        return executeWithRetry(() -> restTemplate.exchange(url, method, requestEntity, responseType), url);
    }

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
                                          ParameterizedTypeReference<T> responseType)
            throws RestClientException {
        return executeWithRetry(() -> restTemplate.exchange(url, method, requestEntity, responseType), url);
    }

    public <T> T getForObject(String url, Class<T> serverTimeResponseClass) {
        return executeWithRetry(() -> restTemplate.getForObject(url, serverTimeResponseClass), url);
    }

    public ResponseEntity<Map> getForEntity(String url, Class<Map> mapClass) {
        return executeWithRetry(() -> restTemplate.getForEntity(url, mapClass), url);
    }

    public <T> T executeWithRetry(Supplier<T> operation, String url) throws RestClientException {
        int attempts = 0;
        long delay = INITIAL_DELAY;

        while (attempts < MAX_RETRIES) {
            try {
                T result = operation.get(); // If successful, return immediately
                log.info("Operation successful on attempt {}. URL: {}", attempts + 1, url);
                return result;
            } catch (ResourceAccessException e) {
                if (e.getCause() instanceof UnknownHostException) {
                    log.error("Unknown host error. URL: {}, Error: {}", url, e.getMessage());
                    attempts++;
                    if (attempts < MAX_RETRIES) {
                        log.info("Retrying request due to UnknownHostException. Attempt {} of {}. URL: {}",
                                attempts + 1, MAX_RETRIES, url);
                        try {
                            Thread.sleep(delay);
                            delay *= BACKOFF_MULTIPLIER;
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RestClientException("Retry interrupted", ie);
                        }
                    } else {
                        log.error("All retries failed due to UnknownHostException. URL: {}", url);
                        throw new RestClientException("Failed to retrieve data after retries due to UnknownHostException", e);
                    }
                } else {
                    // For other ResourceAccessExceptions, don't retry
                    log.error("Resource access error (not retrying). URL: {}, Error: {}", url, e.getMessage());
                    throw e;
                }
            } catch (RestClientException e) {
                // For any other RestClientException, don't retry
                log.error("Rest client error (not retrying). URL: {}, Error: {}", url, e.getMessage());
                throw e;
            }
        }

        // This line should never be reached due to the throw in the else block above
        throw new RestClientException("Failed to retrieve data after retries");
    }

    private void logAndThrowIfFinalAttempt(Exception e, String url, int attempt) throws RestClientException {
        if (attempt == MAX_RETRIES - 1) {
            log.error("All retries failed. URL: {}, Error: {}", url, e.getMessage());
            throw new RestClientException("Failed to retrieve data after retries", e);
        }
    }
}