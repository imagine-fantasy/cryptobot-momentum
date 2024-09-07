package com.crypto.cmtrade.cryptobot.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BinanceMarketTickerClient {

    
    private static final String BINANCE_WS_URL = "wss://data-stream.binance.vision/ws/!ticker@arr";
    private WebSocketSession session;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void connect() {
        StandardWebSocketClient client = new StandardWebSocketClient();

        // Configure the WebSocketClient for larger messages
        client.doHandshake(new BinanceWebSocketHandler(), BINANCE_WS_URL);
        client.setUserProperties(Collections.singletonMap("org.apache.tomcat.websocket.bufferSize", 10485760));
        // Schedule periodic ping messages
        scheduler.scheduleAtFixedRate(this::sendPing, 3, 3, TimeUnit.MINUTES);
    }

    private class BinanceWebSocketHandler extends AbstractWebSocketHandler {
        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            BinanceMarketTickerClient.this.session = session;
            log.info("WebSocket connection established");
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            String payload = message.getPayload();
            handleTickerUpdates(payload);
        }

        @Override
        protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
            // Handle pong message if needed
            log.debug("Received pong message");
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            log.error("WebSocket transport error", exception);
            reconnect();
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            log.info("WebSocket connection closed: {}", status);
            reconnect();
        }

        private void handleTickerUpdates(String payload) {
            try {
                List<Map<String, Object>> tickers = objectMapper.readValue(payload, new TypeReference<List<Map<String, Object>>>() {});
                int chunkSize = 100; // Process 100 tickers at a time
                for (int i = 0; i < tickers.size(); i += chunkSize) {
                    List<Map<String, Object>> chunk = tickers.subList(i, Math.min(i + chunkSize, tickers.size()));
                    processChunk(chunk);
                }
            } catch (IOException e) {
                log.error("Error parsing ticker data", e);
            }
        }

        private void processChunk(List<Map<String, Object>> chunk) {
            List<Map<String, Object>> usdtPairs = chunk.stream()
                    .filter(ticker -> ((String) ticker.get("s")).endsWith("USDT"))
                    .collect(Collectors.toList());
            usdtPairs.sort((a, b) -> {
                double changeA = Double.parseDouble((String) a.get("P"));
                double changeB = Double.parseDouble((String) b.get("P"));
                return Double.compare(changeB, changeA); // Descending order
            });
            logTop20USDTPairs(usdtPairs);
        }

        private void logTop20USDTPairs(List<Map<String, Object>> usdtPairs) {
            log.info("Top 20 USDT pairs by 24h percent change:");
            for (int i = 0; i < Math.min(20, usdtPairs.size()); i++) {
                Map<String, Object> ticker = usdtPairs.get(i);
                String symbol = (String) ticker.get("s");
                String percentChange = (String) ticker.get("P");
                String lastPrice = (String) ticker.get("c");
                log.info("{}. {} - Change: {}%, Last Price: {}", i+1, symbol, percentChange, lastPrice);
            }
        }
    }

    private void sendPing() {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new PingMessage());
                log.debug("Sent ping message");
            } catch (IOException e) {
                log.error("Error sending ping message", e);
            }
        }
    }

    private void reconnect() {
        log.info("Attempting to reconnect...");
        scheduler.schedule(this::connect, 5, TimeUnit.SECONDS);
    }
}