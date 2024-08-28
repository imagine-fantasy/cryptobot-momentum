package com.crypto.cmtrade.cryptobot.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

public class RetryableRestTemplate extends RestTemplate {
    private static final Logger logger = Logger.getLogger(RetryableRestTemplate.class.getName());
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1 second

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws RestClientException {
        Exception lastException = null;
        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            try {
                return super.exchange(url, method, requestEntity, responseType, uriVariables);
            } catch (ResourceAccessException e) {
                lastException = e;
                logger.warning("Request failed (attempt " + (retry + 1) + "): " + e.getMessage());
                if (retry < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RestClientException("Thread interrupted", ie);
                    }
                }
            }
        }
        throw new RestClientException("Failed after " + MAX_RETRIES + " attempts", lastException);
    }
}