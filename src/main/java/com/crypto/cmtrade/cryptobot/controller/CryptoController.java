package com.crypto.cmtrade.cryptobot.controller;

import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.crypto.cmtrade.cryptobot.service.DataFetcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final DataFetcherService dataFetcherService;

    @Autowired
    public CryptoController(DataFetcherService dataFetcherService) {
        this.dataFetcherService = dataFetcherService;
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
}