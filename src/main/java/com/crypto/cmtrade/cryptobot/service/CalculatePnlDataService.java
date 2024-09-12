package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

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


    @Autowired
    private CryptoTopNCurrentService cryptoTopNCurrentService;

    @Autowired
    CryptoTrackingSummaryService cryptoTrackingSummaryService;

    @Autowired
    DynamicRebalanceService dynamicRebalanceService;


    @Autowired
    private PlatformTransactionManager transactionManager;

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.MINUTES)
    public void calculatePnl(){
        log.info("CalculatePnl started...");
        CryptoTrackingSummary beforePNLProcess = cryptoTrackingSummaryService.getMostRecent();

        log.info(" beforePNLProcess CryptoTrackingSummaryId, pnlSummaryId {},{}",beforePNLProcess.getId(),beforePNLProcess.getSummaryId());

        try {

            log.info("calculating pnl Summary process Intiated");
            PnlSummary pnlSummary=new PnlSummary();
            pnlSummary=pnlSummaryService.savePnlSummary(pnlSummary);
            List<CryptoData> cryptoData = dataFetcherService.fetchAllCrypto();
            Map<String, CryptoData> priceMap = cryptoData.stream()
                    .collect(Collectors.toMap(CryptoData::getSymbol, cd -> cd));
            LocalDateTime currentTime = LocalDateTime.now();
            saveTopNCrytpos(cryptoData, currentTime);
            List<CryptoPortfolio> currentHoldings = cryptoPortfolioService.getAllCryptoPortfolios();

            BigDecimal totalCostBasis= new BigDecimal(BigInteger.ZERO);
            BigDecimal totalUnrealizedPnl=new BigDecimal(BigInteger.ZERO);
            BigDecimal totalCurrentValue=new BigDecimal(BigInteger.ZERO);

            for (CryptoPortfolio portfolio : currentHoldings) {
                CryptoData currentData = priceMap.get(portfolio.getSymbol());
                if (currentData != null && portfolio.getQuantity()!=null) {
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
            log.info("calculating for portfolio profit completed");
             savePnlSummary(pnlSummary,currentTime, totalUnrealizedPnl, totalCurrentValue, totalCostBasis);



            log.info("Successfully calculated pnl for portfolios");

//            cryptoTrackingSummaryService.callAfterPnlSummaryInsert();

            CryptoTrackingSummary afterPNLProcess = cryptoTrackingSummaryService.getMostRecent();
            if(afterPNLProcess.getId().compareTo(beforePNLProcess.getId())>0 ){
                log.info(" Dynamically Rebalanced initiated ");
                dynamicRebalanceService.checkAndRebalance();
                log.info(" Dynamically Rebalanced completed  ");
            }

            log.info(" afterPNLProcess CryptoTrackingSummaryId, pnlSummaryId {},{}",afterPNLProcess.getId(),afterPNLProcess.getSummaryId());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("exception calculating pnl for current cycle: {}", e.getMessage());
        }


    }

    private PnlSummary calculateAndSavePnlSummary() {
        log.info("calculating pnl Summary process Intiated");
        PnlSummary pnlSummary=new PnlSummary();
        pnlSummary=pnlSummaryService.savePnlSummary(pnlSummary);
        List<CryptoData> cryptoData = dataFetcherService.fetchAllCrypto();
        Map<String, CryptoData> priceMap = cryptoData.stream()
                .collect(Collectors.toMap(CryptoData::getSymbol, cd -> cd));
        LocalDateTime currentTime = LocalDateTime.now();
        saveTopNCrytpos(cryptoData, currentTime);
        List<CryptoPortfolio> currentHoldings = cryptoPortfolioService.getAllCryptoPortfolios();

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
        log.info("calculating for portfolio profit completed");
        return savePnlSummary(pnlSummary,currentTime, totalUnrealizedPnl, totalCurrentValue, totalCostBasis);
    }

    private void saveTopNCrytpos(List<CryptoData> cryptoData, LocalDateTime currentTime) {
        cryptoTopNCurrentService.deleteAllCryptoTopNCurrent();

        cryptoData.stream().limit(20).forEach(data ->{
            CryptoTopNCurrent cryptoTopNCurrent=new CryptoTopNCurrent();
            cryptoTopNCurrent.setCryptoCurrency(data.getSymbol());
            cryptoTopNCurrent.setLastPrice(data.getPrice());
            cryptoTopNCurrent.setMarketCap(data.getMarketCap());
            cryptoTopNCurrent.setLastUpdated(currentTime);
            saveCryptoTopNCurrent(cryptoTopNCurrent);

        });
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
    private PnlSummary savePnlSummary(PnlSummary pnlSummary, LocalDateTime now, BigDecimal totalUnrealizedPnl, BigDecimal totalCurrentValue, BigDecimal totalCostBasis) {
        log.info("Total Costbasis {}, unrealized Pnl {}, total Current Value {}",totalCostBasis,totalUnrealizedPnl,totalCurrentValue);
        pnlSummary.setTotalUnrealizedPnl(totalUnrealizedPnl);
        pnlSummary.setTotalCurrentValue(totalCurrentValue);
        pnlSummary.setTotalCostBasis(totalCostBasis);
        pnlSummary.setTimestamp(now);

        return pnlSummaryService.savePnlSummary(pnlSummary);

    }


    private void saveCryptoTopNCurrent(CryptoTopNCurrent pnlTopNCurrent){

        cryptoTopNCurrentService.saveCryptoPortfolio(pnlTopNCurrent);
    }

}
