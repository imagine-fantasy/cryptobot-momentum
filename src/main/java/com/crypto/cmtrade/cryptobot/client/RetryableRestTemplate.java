package com.crypto.cmtrade.cryptobot.client;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.function.Supplier;


@Slf4j
@Data
@Component
public class RetryableRestTemplate {

    private RestTemplate restTemplate ;

    public RetryableRestTemplate() {
        this.restTemplate = new RestTemplate();
    }


    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables)
            throws RestClientException {
            return executeWithRetry(() ->restTemplate.exchange(url, method, requestEntity, responseType, uriVariables), url);
    }


    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType)
            throws RestClientException {
        return executeWithRetry(() -> restTemplate.exchange(url, method, requestEntity, responseType),url);
    }





    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
                                          ParameterizedTypeReference<T> responseType)
            throws RestClientException {
        return executeWithRetry(()-> restTemplate.exchange(url, method, requestEntity, responseType),url);
    }

    public <T> T getForObject(String url, Class<T> serverTimeResponseClass) {

        return executeWithRetry(()->  restTemplate.getForObject(url, serverTimeResponseClass),url);
    }
    // You can add more methods as needed, wrapping other RestTemplate methods

    // This method allows access to the underlying RestTemplate if needed


    @Retryable(
            value = {ResourceAccessException.class, UnknownHostException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public <T> T executeWithRetry(Supplier<T> operation, String url) throws RestClientException {
        try {
            return operation.get();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                log.error("Bad Request (400) error. Not retrying. URL: {}, Error: {}", url, e.getMessage());
                throw e; // Don't retry 400 errors
            }
            throw e; // Let other client errors be handled by @Recover
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof UnknownHostException) {
                log.error("Unknown host error. URL: {}, Error: {}", url, e.getMessage());
            } else {
                log.error("Resource access error. URL: {}, Error: {}", url, e.getMessage());
            }
            throw e; // Let it be retried
        } catch (HttpServerErrorException e) {
            log.error("Server error. Retrying. URL: {}, Status: {}, Error: {}", url, e.getStatusCode(), e.getMessage());
            throw e; // Let it be retried
        }
    }

    public ResponseEntity<Map> getForEntity(String url, Class<Map> mapClass) {
        return executeWithRetry(()-> restTemplate.getForEntity(url, mapClass),url);
    }

    @Recover
    public <T> T recover(Exception e, Supplier<T> operation, String url) {
        log.error("All retries failed. URL: {}, Error: {}", url, e.getMessage());
        throw new RestClientException("Failed to retrieve data after retries", e);
    }
}