package com.crypto.cmtrade.cryptobot.client;

import com.crypto.cmtrade.cryptobot.event.CryptoDataUpdateEvent;
import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.crypto.cmtrade.cryptobot.util.UtilConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.time.Duration;

import java.io.IOException;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BinanceMarketTickerClient   implements WebSocketListener{

    //    private static final String BINANCE_WS_URL = "wss://data-stream.binance.vision/ws/!ticker@arr";
//    private static final String BINANCE_WS_URL = "wss://data-stream.binance.vision/ws/!ticker_4h@arr@3000ms";
        private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/!ticker_4h@arr@3000ms";
//        private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/!ticker@arr@3000ms";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final BlockingDeque<String> messageStack = new LinkedBlockingDeque<>();
//    private final ExecutorService messageProcessor = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledExecutorService messageProcessor = Executors.newScheduledThreadPool(1);
//    private final ExecutorService messageProcessor = Executors.newSingleThreadScheduledExecutor();
    private Session session;
    private WebSocketClient client;
    private StringBuilder partialMessage = new StringBuilder();


    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public BinanceMarketTickerClient(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void connect() {
        try {
            client = new WebSocketClient();
            client.setMaxTextMessageSize(100* 1024 * 1024); // 100MB buffer
            client.setIdleTimeout(Duration.ofSeconds(30));
            client.start();
            client.connect(this, new URI(BINANCE_WS_URL)).get(10, TimeUnit.SECONDS);
            startMessageProcessing();
            scheduler.scheduleAtFixedRate(this::sendPing, 3, 3, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Error establishing WebSocket connection", e);
            reconnect();
        }
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        log.info("WebSocket connection established");
    }

    @Override
    public void onWebSocketText(String message) {
//        messageQueue.offer(message);
        messageStack.clear();
        messageStack.push(message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        log.info("WebSocket connection closed: {} - {}", statusCode, reason);
        reconnect();
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        log.error("WebSocket error", cause);
        reconnect();
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        // Not used in this implementation
    }

    private void startMessageProcessing() {
        messageProcessor.scheduleWithFixedDelay(()->{

                try {
                    log.info(" message stack size is {}", messageStack.size());
//                    String message = messageQueue.take();
                    String message= messageStack.pollLast();
                    messageStack.clear();
                    if (message != null){
                        handleTickerUpdates(message);
                    }

                }  catch (Exception e) {
                    log.error("Error processing message", e);
                }

        },3,30,TimeUnit.SECONDS);
//        messageProcessor.submit(() -> {
//            while (!Thread.currentThread().isInterrupted()) {
//                try {
//
////                    String message = messageQueue.take();
//                    String message= messageStack.pollLast();
//                    if (message != null){
//                        handleTickerUpdates(message);
//                    }
//
//                }  catch (Exception e) {
//                    log.error("Error processing message", e);
//                }
//            }
//        });
    }

    private void handleTickerUpdates(String payload) {
        try {
//            System.out.println("payload "+ payload);
            var tickers = objectMapper.readValue(payload, new TypeReference<List<Map<String, Object>>>() {});
//            var usdtPairs = tickers.stream()
//                    .filter(ticker -> ((String) ticker.get("s")).endsWith("USDT"))
//                    .sorted((a, b) -> Double.compare(
//                            Double.parseDouble((String) b.get("P")),
//                            Double.parseDouble((String) a.get("P"))
//                    ))
//                    .limit(20)
//                    .toList();


            Map<String, CryptoData> allCryptoDataMap = tickers.parallelStream()
                    .filter(ticker -> ((String) ticker.get("s")).endsWith("USDT"))
                    .collect(Collectors.toConcurrentMap(
                            ticker -> (String) ticker.get("s"),
                            this::toCryptoData
                    ));

            AtomicInteger rank = new AtomicInteger(1);
            List<CryptoData> top20CryptoData = allCryptoDataMap.values().stream()
                    .sorted(Comparator.comparing(CryptoData::getPriceChangePercent).reversed())
                    .limit(UtilConstants.DEFAULT_SIZE)
                    .peek(crypto -> crypto.setRank(rank.getAndIncrement()))
                    .toList();

            logTop20USDTPairs(top20CryptoData);

            eventPublisher.publishEvent(new CryptoDataUpdateEvent(this,allCryptoDataMap,top20CryptoData));
            log.info("Top 20 Sleeping for 10 seconds ");
//            Thread.sleep(30000);
            log.info("Completed top20 after 10 seconds");


        } catch (Exception e) {
            log.error("Error parsing ticker data", e);
        }
    }

    private void logTop20USDTPairs(List<CryptoData> cryptoData){
        log.info("Top 20 USDT pairs by 24h percent change:");
        for (int i = 0; i < cryptoData.size(); i++) {
            var ticker = cryptoData.get(i);
            var symbol = ticker.getSymbol();
            var percentChange = ticker.getPriceChangePercent();
            var lastPrice = ticker.getPrice();
            log.info("{}. {} - Change: {}%, Last Price: {}", i+1, symbol, percentChange, lastPrice);
        }
    }

    private void sendPing() {
        if (session != null && session.isOpen()) {
            try {
                System.currentTimeMillis();
                session.getRemote().sendPing(ByteBuffer.wrap("ping".getBytes()));
                log.debug("Sent ping message");
            } catch (Exception e) {
                log.error("Error sending ping message", e);
            }
        }
    }

    private void reconnect() {
        log.info("Attempting to reconnect...");
//        scheduler.schedule(this::connect, Duration.ofSeconds(5));
        scheduler.schedule(this::connect, 3, TimeUnit.SECONDS);
    }

    public void shutdown() {
        if (session != null && session.isOpen()) {
            session.close();
        }
        try {
            client.stop();
        } catch (Exception e) {
            log.error("Error stopping WebSocket client", e);
        }
        scheduler.shutdownNow();
        messageProcessor.close();
    }

    private CryptoData toCryptoData(Map<String, Object> ticker) {
        return new CryptoData(
                (String) ticker.get("s"),
                ((String) ticker.get("s")).replace("USDT", ""),
                new BigDecimal((String) ticker.get("c")),
                new BigDecimal((String) ticker.get("P")), // Assuming 1h percent change
                new BigDecimal((String) ticker.get("v"))
                ,null
        );
    }
}