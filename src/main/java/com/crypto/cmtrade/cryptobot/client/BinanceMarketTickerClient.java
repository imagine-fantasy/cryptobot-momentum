package com.crypto.cmtrade.cryptobot.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
public class BinanceMarketTickerClient {

//    private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/!ticker@arr";
    @Value("${binancetest}")
    private  String BINANCE_WS_URL ;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CountDownLatch closeLatch = new CountDownLatch(1);

    public void connect() {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketHandler handler = new BinanceWebSocketHandler();

        client.doHandshake(handler, BINANCE_WS_URL);

        log.info("Connected to Binance WebSocket");

        // Keep the application running
        try {
            closeLatch.await();
        } catch (InterruptedException e) {
            log.error("WebSocket connection interrupted", e);
        }
    }

    private class BinanceWebSocketHandler extends TextWebSocketHandler {
        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            log.info("WebSocket connection established");
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            try {
                JsonNode jsonNode = objectMapper.readTree(message.getPayload());
                for (JsonNode ticker : jsonNode) {
                    String symbol = ticker.get("s").asText();
                    String priceChange = ticker.get("p").asText();
                    String priceChangePercent = ticker.get("P").asText();
                    String lastPrice = ticker.get("c").asText();
                    String volume = ticker.get("v").asText();

                    log.info("Symbol: {}, Price Change: {}, Change %: {}%, Last Price: {}, Volume: {}",
                            symbol, priceChange, priceChangePercent, lastPrice, volume);

                    // Here you can add logic to identify top 20 by percent change
                    // and trigger your trading strategy when necessary
                }
            } catch (Exception e) {
                log.error("Error processing WebSocket message", e);
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            log.error("WebSocket transport error", exception);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
            log.info("WebSocket connection closed: {}", status);
            closeLatch.countDown();
        }
    }
}