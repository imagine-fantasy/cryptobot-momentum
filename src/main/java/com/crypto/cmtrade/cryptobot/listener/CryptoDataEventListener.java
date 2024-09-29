package com.crypto.cmtrade.cryptobot.listener;

import com.crypto.cmtrade.cryptobot.event.CryptoDataUpdateEvent;
import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.crypto.cmtrade.cryptobot.service.CalculatePnlDataService;
import com.crypto.cmtrade.cryptobot.service.PortfolioInitializationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CryptoDataEventListener {


    @Autowired
    private CalculatePnlDataService pnlDataService;

    @Autowired
    PortfolioInitializationService portfolioInitializationService;

    @EventListener
    public void handleCryptoDataUpdate(CryptoDataUpdateEvent event) {
        Map<String, CryptoData> allData = event.getAllCryptoDataMap();
        List<CryptoData> top20 = event.getTop20CryptoData();
        // Use the updated data...
        if (top20!=null && top20.size()>=20){
            log.info("Event Listener Services Process started {}", top20.size());
            portfolioInitializationService.initializePortfolioIfNeeded(allData,top20);
            pnlDataService.calculatePnl(allData,top20);
            log.info("Event Listener Services Process completed {}",top20.size());
        }







    }
}