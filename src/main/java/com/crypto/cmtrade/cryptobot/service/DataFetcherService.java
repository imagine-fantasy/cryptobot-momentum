package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.client.BinanceApiClient;
import com.crypto.cmtrade.cryptobot.client.CoinMarketCapApiClient;
import com.crypto.cmtrade.cryptobot.model.CryptoData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class DataFetcherService {
    private final BinanceApiClient binanceApiClient;

    @Autowired
    public DataFetcherService(BinanceApiClient binanceApiClient) {
        this.binanceApiClient = binanceApiClient;
    }

    public BigDecimal getAccountBalance(){
        return  binanceApiClient.getAccountBalance();
    }


    public List<CryptoData> fetchTop20Cryptocurrencies() {
        return binanceApiClient.fetchTop20CoinsByPercentChangeOnBinance();
    }
    public Map<String,Object> getAccountInfo () throws InterruptedException {
        return binanceApiClient.getAccountInfo();
    }
}