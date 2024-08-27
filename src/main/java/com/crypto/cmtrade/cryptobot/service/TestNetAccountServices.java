package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.client.BinanceApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class TestNetAccountServices {
    @Autowired
    private BinanceApiClient binanceApiClient;


    public boolean resetAccount(){
        return binanceApiClient.resetTestnetAccountBalance();
    }

    public Map<String, Object> getOrderBookDepth(String symbol, int limit){
        return binanceApiClient.getOrderBookDepth(symbol, limit);
    }
}
