package com.crypto.cmtrade.cryptobot.client;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

;
@Log4j2
public class RetryableRestTemplate extends RestTemplate {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1 second

    public RetryableRestTemplate(){
        super();
        log.info("RetryableRestTemplate initialized");
    }

    @NotNull
    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws RestClientException {
        Exception lastException = null;
        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            try {
                log.info("Exchange count retry {} ,",retry);
                return super.exchange(url, method, requestEntity, responseType, uriVariables);
            } catch (ResourceAccessException e) {
                lastException = e;
                log.error("Request failed (attempt " + (retry + 1) + "): " + e.getMessage());
                if (retry < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RestClientException("Thread interrupted", ie);
                    }
                }
            }
            catch (Exception e){
                log.error(" exception while connecting to resource {}",e.getMessage());
                throw new RestClientException("Thread interrupted", e);
            }
        }
        throw new RestClientException("Failed after " + MAX_RETRIES + " attempts", lastException);
    }
}