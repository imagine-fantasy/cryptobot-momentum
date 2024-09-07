package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import com.crypto.cmtrade.cryptobot.model.PnlData;
import com.crypto.cmtrade.cryptobot.model.PnlSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CalculatePnlDataService {

    @Autowired
    private DataFetcherService dataFetcherService;

    @Autowired
    private PnlDataService pnlDataService;


    @Autowired
    private CryptoPortfolioService cryptoPortfolioService;

    @Autowired
    private PnlSummaryService pnlSummaryService;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void calculatePnl(){
        log.info("CalculatePnl started...");

        PnlSummary pnlSummary=new PnlSummary();
        pnlSummary=pnlSummaryService.savePnlSummary(pnlSummary);
        try {
            List<CryptoData> cryptoData = dataFetcherService.fetchAllCrypto();
            Map<String, CryptoData> priceMap = cryptoData.stream()
                    .collect(Collectors.toMap(CryptoData::getSymbol, cd -> cd));


            List<CryptoPortfolio> currentHoldings = cryptoPortfolioService.getAllCryptoPortfolios();
            LocalDateTime currentTime = LocalDateTime.now();
            BigDecimal totalCostBasis= new BigDecimal(BigInteger.ZERO);
            BigDecimal totalUnrealizedPnl=new BigDecimal(BigInteger.ZERO);
            BigDecimal totalCurrentValue=new BigDecimal(BigInteger.ZERO);

            for (CryptoPortfolio portfolio : currentHoldings) {
                CryptoData currentData = priceMap.get(portfolio.getSymbol());
                if (currentData != null) {
                    BigDecimal currentValue = currentData.getPrice().multiply(portfolio.getQuantity());
                    BigDecimal costBasis = portfolio.getLastPrice().multiply(portfolio.getQuantity());
                    BigDecimal unrealizedPNL = currentValue.subtract(costBasis);
                    portfolio.setLastKnownPnl(unrealizedPNL);
                    portfolio.setPnlUpdatedAt(currentTime);

                    savePNLData(portfolio.getSymbol(), unrealizedPNL, currentData.getPrice(), currentTime,costBasis,pnlSummary.getSummaryId());
                    cryptoPortfolioService.saveCryptoPortfolio(portfolio);
                    totalCostBasis=totalCostBasis.add(costBasis);
                    totalUnrealizedPnl=totalUnrealizedPnl.add(unrealizedPNL);
                    totalCurrentValue=totalCurrentValue.add(currentValue);

                }
            }

            savePnlSummary(pnlSummary,currentTime, totalUnrealizedPnl, totalCurrentValue, totalCostBasis);
            log.info("Successfully calculated pnl for portfolios");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("exception calculating pnl for current cycle: {}", e.getMessage());
        }


    }



    private void savePNLData(String symbol, BigDecimal unrealizedPNL, BigDecimal price, LocalDateTime now,BigDecimal costBasis, BigInteger summaryId) {

        PnlData  pnlData=new PnlData();
        pnlData.setSymbol(symbol);
        pnlData.setUnrealizedPnl(unrealizedPNL);
        pnlData.setLastUpdated(now);
        pnlData.setCurrentPrice(price);
        pnlData.setSummaryId(summaryId);
        pnlDataService.savePnlData(pnlData);
    }
    private void savePnlSummary(PnlSummary pnlSummary, LocalDateTime now, BigDecimal totalUnrealizedPnl, BigDecimal totalCurrentValue, BigDecimal totalCostBasis) {
        pnlSummary.setTotalUnrealizedPnl(totalUnrealizedPnl);
        pnlSummary.setTotalCurrentValue(totalCurrentValue);
        pnlSummary.setTotalCostBasis(totalCostBasis);
        pnlSummary.setTimestamp(now);
        pnlSummaryService.savePnlSummary(pnlSummary);
    }

}
