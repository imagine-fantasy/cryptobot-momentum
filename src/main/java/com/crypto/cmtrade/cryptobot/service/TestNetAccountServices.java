package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.client.BinanceApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestNetAccountServices {
    @Autowired
    private BinanceApiClient binanceApiClient;


    public boolean resetAccount(){
        return binanceApiClient.resetTestnetAccountBalance();
    }

}
