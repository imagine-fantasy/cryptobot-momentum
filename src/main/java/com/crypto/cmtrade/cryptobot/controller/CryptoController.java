package com.crypto.cmtrade.cryptobot.controller;

import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import com.crypto.cmtrade.cryptobot.service.*;
import com.crypto.cmtrade.cryptobot.util.OrderSide;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final DataFetcherService dataFetcherService;

    private final TestNetAccountServices testNetAccountServices;

    private  final TradeService tradeService;

    private  final CryptoPortfolioService cryptoPortfolioService;

    private  final ApplicationRestartService applicationRestartService;
    @Autowired
    public CryptoController(DataFetcherService dataFetcherService, TestNetAccountServices testNet, TradeService tradeService, CryptoPortfolioService cryptoPortfolioService, ApplicationRestartService applicationRestartService) {
        this.dataFetcherService = dataFetcherService;
        this.testNetAccountServices= testNet;
        this.tradeService = tradeService;
        this.cryptoPortfolioService = cryptoPortfolioService;
        this.applicationRestartService = applicationRestartService;
    }


    @GetMapping("/top20")
    public ResponseEntity<List<CryptoData>> getTop20Cryptocurrencies() {
        List<CryptoData> top20 = dataFetcherService.fetchTop20Cryptocurrencies();
        return ResponseEntity.ok(top20);
    }

    @GetMapping("/getAccountBalance")
    public ResponseEntity<Map<String,Object>> getAccountInfo() throws InterruptedException {
        Map<String, Object> accountInfo = dataFetcherService.getAccountInfo();
        return ResponseEntity.ok(accountInfo);
    }

    @GetMapping("/resetAccount")
    public ResponseEntity<Map<String,Object>> resetAccount() throws InterruptedException {
        try{
            boolean isResetCompleted= testNetAccountServices.resetAccount();
        } catch (Exception e) {

        }
        Map<String, Object> accountInfo = dataFetcherService.getAccountInfo();
        return ResponseEntity.ok(accountInfo);

    }

    @GetMapping("/sellAll")
    public ResponseEntity<String> sellAllCrypto() throws InterruptedException {

        List<CryptoPortfolio> portfolios = cryptoPortfolioService.findAllBySymbolActiveHolding();

        for (CryptoPortfolio portfolio :portfolios){
            tradeService.executeTrade(portfolio.getBatchId(),portfolio.getSymbol(), OrderSide.SELL,portfolio.getAmount(),portfolio.getLastPrice(),portfolio.getQuantity());
        }


        return ResponseEntity.ok("Completed successfully");

    }

    @GetMapping("/sell/{symbol}")
    public ResponseEntity<String> sellCryptoSymbol(@PathVariable("symbol") String symbol) throws InterruptedException {

        Optional<CryptoPortfolio> cryptoPortfolio = cryptoPortfolioService.findBySymbol(symbol);
        cryptoPortfolio.ifPresent( portfolio -> {
            tradeService.executeTrade(portfolio.getBatchId(),portfolio.getSymbol(), OrderSide.SELL,portfolio.getQuantity(), BigDecimal.ZERO,portfolio.getQuantity());
        });

        return ResponseEntity.ok("Completed successfully");

    }

    @GetMapping("/depth/{symbol}/{limit}")
    public ResponseEntity<?> getOrderBookDepth(@PathVariable String symbol, @PathVariable int limit) {
        try {
            Map<String, Object> depth = testNetAccountServices.getOrderBookDepth(symbol, limit);
            return ResponseEntity.ok(depth);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching order book depth: " + e.getMessage());
        }
    }

    @GetMapping("/restart")
    public ResponseEntity<?> restartApp() {
        try {

            applicationRestartService.restartApplication();

            return ResponseEntity.ok("");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching order book depth: " + e.getMessage());
        }
    }


}