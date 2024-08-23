package com.crypto.cmtrade.cryptobot.client;

import com.crypto.cmtrade.cryptobot.model.CryptoData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BinanceApiClient {
    private final String apiKey;
    private final String secretKey;
    private final RestTemplate restTemplate;
    private final String baseUrl; // Use this for testnet
    private static final long CACHE_DURATION_MS = 30000; // 30 seconds
    private long cachedServerTime = 0;
    private long lastFetchTime = 0;

    public BinanceApiClient(@Value("${binance.api.key}") String apiKey,
                            @Value("${binance.secret.key}") String secretKey, @Value("${binance.base_url}") String baseUrl) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.baseUrl = baseUrl;

        this.restTemplate = new RestTemplate();
    }

    public String createListenKey() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-MBX-APIKEY", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v3/userDataStream",
                HttpMethod.POST,
                entity,
                Map.class
        );

        return (String) response.getBody().get("listenKey");
    }

    public long getServerTime() {
      long currentTime = System.currentTimeMillis();
        if (currentTime - lastFetchTime > CACHE_DURATION_MS) {
            // Cache expired, fetch new server time
            String timeEndpoint = baseUrl + "/v3/time";
            ServerTimeResponse response = restTemplate.getForObject(timeEndpoint, ServerTimeResponse.class);
            assert response != null;
            cachedServerTime = response.getServerTime();
            lastFetchTime = currentTime;
        }
        return cachedServerTime + (currentTime - lastFetchTime);


    }

    public Map<String, Object> getAccountInfo()  {


        long timestamp = getServerTime();
        log.info("before timestamp: " + timestamp );

        long adjustedTimeStamp =Math.subtractExact(timestamp, 1000);

        log.info( "timestamp: " + timestamp + " adjusted timeStamp: " + adjustedTimeStamp);
        String queryString = "timestamp=" + timestamp;
        String signature = generateSignature(queryString);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-MBX-APIKEY", apiKey);

        String url = baseUrl + "/v3/account?" + queryString + "&signature=" + signature;

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        return response.getBody();
    }

    public Map<String, Double> getBalances()  {
        Map<String, Object> accountInfo = getAccountInfo();

        List<Map<String, Object>> balances = (List<Map<String, Object>>) accountInfo.get("balances");

        Map<String, Double> result = new HashMap<>();
        for (Map<String, Object> balance : balances) {
            String asset = (String) balance.get("asset");
            double free = Double.parseDouble((String) balance.get("free"));
            if (free > 0) {
                result.put(asset, free);
            }
        }

        return result;
    }

    private String generateSignature(String data) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Unable to sign message", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }



    public List<CryptoData> fetchTop20CoinsByPercentChangeOnBinance() {
        String url = baseUrl + "/v3/ticker/24hr";

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,  // No HttpEntity needed as this is a public endpoint
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> allTickers = response.getBody();

        assert allTickers != null;
        return allTickers.stream()
                .filter(ticker -> {
                  log.info("Filter about to be applied to symbol"+ ((String) ticker.get("symbol"))+((String) ticker.get("symbol")).endsWith("USDT"));
                    return   ((String) ticker.get("symbol")).endsWith("USDT");

                } ) // Filter for USDT pairs
                .sorted((a, b) -> Double.compare(
                        Double.parseDouble((String) b.get("priceChangePercent")),
                        Double.parseDouble((String) a.get("priceChangePercent"))
                ))
                .limit(20)
                .map(this::toCryptoData)
                .collect(Collectors.toList());
    }

    private CryptoData toCryptoData(Map<String, Object> ticker) {
        return new CryptoData(
                (String) ticker.get("symbol"),
                (String) ((String) ticker.get("symbol")).replace("USDT", ""), // Remove USDT from symbol for base asset
                new BigDecimal((String) ticker.get("lastPrice")),
                new BigDecimal((String) ticker.get("priceChangePercent"))
        );
    }

    // Add other methods for placing orders, etc.

    @Setter
    @Getter
    private static class ServerTimeResponse {
        private long serverTime;

    }
}