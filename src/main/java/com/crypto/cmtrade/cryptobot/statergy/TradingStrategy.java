package com.crypto.cmtrade.cryptobot.statergy;

import com.crypto.cmtrade.cryptobot.model.CryptoData;

import java.util.List;
import java.util.Map;

public interface TradingStrategy {


    public void execute(Map<String, CryptoData> allData, List<CryptoData> top20);
}
