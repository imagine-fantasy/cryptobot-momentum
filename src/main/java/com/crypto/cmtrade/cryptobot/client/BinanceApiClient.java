package com.crypto.cmtrade.cryptobot.client;

import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.crypto.cmtrade.cryptobot.model.OrderResponse;
import com.crypto.cmtrade.cryptobot.util.OrderSide;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BinanceApiClient {
    private final String apiKey;
    private final String secretKey;
//    private final RestTemplate restTemplate;
    private final  RetryableRestTemplate restTemplate;
    private final String baseUrl; // Use this for testnet
    private static final long CACHE_DURATION_MS = 30000; // 30 seconds
    private long cachedServerTime = 0;
    private long lastFetchTime = 0;
    private final Map<String, SymbolInfo> symbolInfoCache = new ConcurrentHashMap<>();

    public BinanceApiClient(@Value("${binance.api.key}") String apiKey,
                            @Value("${binance.secret.key}") String secretKey, @Value("${binance.base_url}") String baseUrl) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.baseUrl = baseUrl;

        this.restTemplate = new RetryableRestTemplate();
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

        return (String) Objects.requireNonNull(response.getBody()).get("listenKey");
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

    public BigDecimal getAccountBalance() {


        try {
            long timestamp = getServerTime();
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


            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> account = response.getBody();
                for (Map<String, Object> balance : (Iterable<Map<String, Object>>) account.get("balances")) {
                    if ("USDT".equals(balance.get("asset"))) {
                        return new BigDecimal((String) balance.get("free"));
                    }
                }
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error getting account balance: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public boolean resetTestnetAccountBalance() {
        String endpoint = "/v1/account/reset";

        try {
            long timestamp = getServerTime();
            String queryString = "timestamp=" + timestamp;
            String signature = generateSignature(queryString);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-MBX-APIKEY", apiKey);

            String url = baseUrl + endpoint + "?" + queryString + "&signature=" + signature;

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Testnet account balance reset successfully");
                return true;
            } else {
                log.error("Failed to reset testnet account balance. Status: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Error resetting testnet account balance: " + e.getMessage());
            return false;
        }
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
                    return   ((String) ticker.get("symbol")).endsWith("USDT");

                } ) // Filter for USDT pairs
                .sorted((a, b) -> Double.compare(
                        Double.parseDouble((String) b.get("priceChangePercent")),
                        Double.parseDouble((String) a.get("priceChangePercent"))
                ))
                .limit(20)
                .map(this:: toCryptoData)
                .collect(Collectors.toList());
    }



    private CryptoData toCryptoData(Map<String, Object> ticker) {
        return new CryptoData(
                (String) ticker.get("symbol"),
                ((String )ticker.get("symbol")).replace("USDT", ""),// Binance doesn't provide names, so we use symbol
                new BigDecimal((String) ticker.get("lastPrice")),
                new BigDecimal((String) ticker.get("priceChangePercent")),
                new BigDecimal((String) ticker.get("volume"))
        );
    }

    // Add other methods for placing orders, etc.
    public OrderResponse placeOrder(String symbol, BigDecimal amount, OrderSide side, BigDecimal quantity) {


        String endpoint = "/v3/order";
        String queryString= null;

        if (side == OrderSide.BUY) {
            queryString = String.format("symbol=%s&side=%s&type=MARKET&quoteOrderQty=%s",
                    symbol, side, amount.toPlainString());
        } else {
            queryString = String.format("symbol=%s&side=%s&type=MARKET&quantity=%s",
                    symbol, side, quantity.toPlainString());
        }

        return placeOrder(symbol, queryString, endpoint);
    }

    private OrderResponse placeOrder(String symbol, String queryString, String endpoint) {
        HttpEntity<String> request = createSignedRequest(queryString);
        ResponseEntity<OrderResponse> response =null;
        try {
             response = restTemplate.exchange(
                    baseUrl + endpoint,
                    HttpMethod.POST, request, OrderResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Order placed successfully for {}: {}", symbol, response.getBody());
                return response.getBody();
            } else {
                log.error("Failed to place order for {}: {}", symbol, response.getBody());
                throw new RuntimeException("Failed to place order");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error placing order for " + symbol + ": " + e.getMessage(), e);
            throw new RuntimeException("Error placing order", e);
        }
    }

    public OrderResponse placeBuyOrder(String symbol, BigDecimal amount, OrderSide side) {
        String endpoint = "/v3/order";
        String queryString = String.format("symbol=%s&side=%s&type=MARKET&quoteOrderQty=%s",
                symbol, side, amount.toPlainString());
        return placeOrder(symbol, queryString, endpoint);
    }

    public OrderResponse placeSellOrder (String symbol, BigDecimal quantity, OrderSide side){
        String endpoint = "/v3/order";
        String  queryString = String.format("symbol=%s&side=%s&type=MARKET&quantity=%s",
                symbol, side, quantity.toPlainString());

        return placeOrder(symbol, queryString, endpoint);
    }

    private HttpEntity<String> createSignedRequest(String queryString) {

        String completeQueryString = queryString + "&timestamp=" + getServerTime();
        String signature = generateSignature(completeQueryString);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-MBX-APIKEY", apiKey);

        return new HttpEntity<>(completeQueryString + "&signature=" + signature, headers);
    }

   @Data
    private static class ServerTimeResponse {
        private long serverTime;

    }

    public void initializeSymbolInfo() {
        String endpoint = "/v3/exchangeInfo";
        String url = baseUrl + endpoint;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> symbols = (List<Map<String, Object>>) response.getBody().get("symbols");

                for (Map<String, Object> symbolData : symbols) {
                    String symbol = (String) symbolData.get("symbol");

                    // Only process USDT pairs
                    if (!symbol.endsWith("USDT")) {
                        continue;
                    }

                    List<Map<String, Object>> filters = (List<Map<String, Object>>) symbolData.get("filters");

                    // Find LOT_SIZE filter
                    Optional<Map<String, Object>> lotSizeFilter = filters.stream()
                            .filter(f -> "LOT_SIZE".equals(f.get("filterType")))
                            .findFirst();

                    if (!lotSizeFilter.isPresent()) {
                        log.warn("LOT_SIZE filter not found for {}. Skipping this symbol.", symbol);
                        continue;
                    }

                    // Find MIN_NOTIONAL filter
                    Optional<Map<String, Object>> notionalFilter = filters.stream()
                            .filter(f -> "NOTIONAL".equals(f.get("filterType")))
                            .findFirst();

                    // Use a default value or skip if MIN_NOTIONAL is not found
                    BigDecimal minNotional = notionalFilter
                            .map(f -> new BigDecimal((String) f.get("minNotional")))
                            .orElse(BigDecimal.ZERO);  // or you could use continue to skip this symbol

                    SymbolInfo symbolInfo = new SymbolInfo(
                            symbol,
                            new BigDecimal((String) lotSizeFilter.get().get("minQty")),
                            new BigDecimal((String) lotSizeFilter.get().get("maxQty")),
                            new BigDecimal((String) lotSizeFilter.get().get("stepSize")),
                            minNotional
                    );

                    symbolInfoCache.put(symbol, symbolInfo);
                    log.info("Added symbol info for {}", symbol);
                }

                log.info("Symbol info cache initialized with {} USDT pairs", symbolInfoCache.size());
            } else {
                throw new RuntimeException("Failed to fetch exchange info");
            }
        } catch (Exception e) {
            log.error("Error fetching exchange info: " + e.getMessage());
            throw new RuntimeException("Error fetching exchange info", e);
        }
    }

    public SymbolInfo getSymbolInfo(String symbol) {
        if (symbolInfoCache.isEmpty()) {
            initializeSymbolInfo();
        }
        SymbolInfo symbolInfo = symbolInfoCache.get(symbol);
        if (symbolInfo == null) {
            throw new RuntimeException("Symbol info not found for " + symbol);
        }
        return symbolInfo;
    }

    @Data
    public static class SymbolInfo {
        public final String symbol;
        public final BigDecimal minQty;
        public final BigDecimal maxQty;
        public final BigDecimal stepSize;
        public final BigDecimal minNotional;
        public SymbolInfo(String symbol, BigDecimal minQty, BigDecimal maxQty, BigDecimal stepSize, BigDecimal minNotional) {
            this.symbol = symbol;
            this.minQty = minQty;
            this.maxQty = maxQty;
            this.stepSize = stepSize;
            this.minNotional= minNotional;
        }
    }

    public Map<String, Object> getOrderBookDepth(String symbol, int limit) {
        String endpoint = "/api/v3/depth";
        String url = baseUrl + endpoint + "?symbol=" + symbol + "&limit=" + limit;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-MBX-APIKEY", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        log.info("url is {}",url);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<List<String>> bids = (List<List<String>>) body.get("bids");
                List<List<String>> asks = (List<List<String>>) body.get("asks");

                BigDecimal totalBidQty = bids.stream()
                        .map(bid -> new BigDecimal(bid.get(1)))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalAskQty = asks.stream()
                        .map(ask -> new BigDecimal(ask.get(1)))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> result = new HashMap<>(body);
                result.put("totalBidQuantity", totalBidQty);
                result.put("totalAskQuantity", totalAskQty);
                result.put("topBidPrice", new BigDecimal(bids.get(0).get(0)));
                result.put("topAskPrice", new BigDecimal(asks.get(0).get(0)));

                return result;
            }


            throw new RuntimeException("Failed to fetch order book depth");
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching order book depth: " + e.getStatusCode() + " " + e.getStatusText());
        }

    }
}