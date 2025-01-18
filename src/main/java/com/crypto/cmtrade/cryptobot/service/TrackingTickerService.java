package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.crypto.cmtrade.cryptobot.model.CryptoTopNArchive;
import com.crypto.cmtrade.cryptobot.model.CryptoTopNCurrent;
import com.crypto.cmtrade.cryptobot.util.UtilConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrackingTickerService {

    private final CryptoTopNCurrentService cryptoTopNCurrentService;

    private final CryptoTopNArchiveService cryptoTopNArchiveService;


@Transactional
    public void saveTickers(List<CryptoData> cryptoData){


        log.info("Saving Tickers list...");
        List<CryptoTopNCurrent> cryptoTopNCurrents=new ArrayList<>();
        List<CryptoTopNArchive> cryptoTopNArchives=new ArrayList<>();
        cryptoData.forEach(data ->{
            CryptoTopNCurrent cryptoTopNCurrent=new CryptoTopNCurrent();
            cryptoTopNCurrent.setCryptoCurrency(data.getSymbol());
            cryptoTopNCurrent.setLastPrice(data.getPrice());
            cryptoTopNCurrent.setMarketCap(data.getMarketCap());
            cryptoTopNCurrent.setLastUpdated( LocalDateTime.now());
            cryptoTopNCurrent.setRollingPctChange24h(data.getPriceChangePercent());
            cryptoTopNCurrent.setRank(data.getRank());
            cryptoTopNCurrents.add(cryptoTopNCurrent);

            CryptoTopNArchive cryptoTopNArchive=new CryptoTopNArchive();
            cryptoTopNArchive.setCryptoCurrency(data.getSymbol());
            cryptoTopNArchive.setLastPrice(data.getPrice());
            cryptoTopNArchive.setMarketCap(data.getMarketCap());
            cryptoTopNArchive.setLastUpdated( LocalDateTime.now());
            cryptoTopNArchive.setRollingPctChange24h(data.getPriceChangePercent());
            cryptoTopNArchive.setRank(data.getRank());

            cryptoTopNArchives.add(cryptoTopNArchive);

        });
        log.info("Saving Tickers list... complete, delete started");

        cryptoTopNCurrentService.deleteAllCryptoTopNCurrent();

        log.info("Saving Tickers list... complete, delete completed, save all started ");
          cryptoTopNCurrentService.saveAllCryptoTopNCurrent(cryptoTopNCurrents);
          cryptoTopNArchiveService.saveAllCryptoPortfolios(cryptoTopNArchives);
        log.info("Saving Tickers list... complete, delete completed, save all completed ");
    }

}
