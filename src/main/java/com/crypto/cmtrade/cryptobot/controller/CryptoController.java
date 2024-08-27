package com.crypto.cmtrade.cryptobot.controller;

import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import com.crypto.cmtrade.cryptobot.service.CryptoPortfolioService;
import com.crypto.cmtrade.cryptobot.service.DataFetcherService;
import com.crypto.cmtrade.cryptobot.service.TestNetAccountServices;
import com.crypto.cmtrade.cryptobot.service.TradeService;
import com.crypto.cmtrade.cryptobot.util.OrderSide;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    public CryptoController(DataFetcherService dataFetcherService, TestNetAccountServices testNet, TradeService tradeService, CryptoPortfolioService cryptoPortfolioService) {
        this.dataFetcherService = dataFetcherService;
        this.testNetAccountServices= testNet;
        this.tradeService = tradeService;
        this.cryptoPortfolioService = cryptoPortfolioService;
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


}