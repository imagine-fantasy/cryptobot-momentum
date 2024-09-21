package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.CryptoTrackingSummary;
import com.crypto.cmtrade.cryptobot.statergy.Top20PercentChangeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
public class DynamicRebalanceService {

    private static final BigDecimal PNL_THRESHOLD_PERCENT=new BigDecimal("0.0056");
    private static final BigDecimal PNL_ENTRY_THRESHOLD_PERCENT=new BigDecimal("0.0056");
    private static final BigDecimal SMALL_THRESHOLD=new BigDecimal("0.0006");
    private static final BigDecimal LARGE_THRESHOLD=new BigDecimal("0.002");
    private static final BigDecimal MEDIUM_THRESHOLD=new BigDecimal("0.001");
    private static final BigDecimal EXTREME_SELL_OFF=new BigDecimal("0.025");
    private static final BigDecimal STOP_LOSS_THRESHOLD=new BigDecimal("-0.02");



    //private queue

    private Deque<BigDecimal> pnlQueue=new LinkedList<>();

    @Autowired
    private CryptoTrackingSummaryService cryptoTrackingSummaryService;

    @Autowired
    private Top20PercentChangeStrategy top20PercentChangeStrategy;

    //    @Scheduled(fixedDelay =5, timeUnit = TimeUnit.MINUTES)
    public void checkAndRebalance(){
        CryptoTrackingSummary summary=cryptoTrackingSummaryService.getMostRecent();
        log.info(" Crypto Tracking Summary PNL is {}, for Recorded timeStamp {}", summary.getPnlNonTop20(),summary.getTimestamp());
        if(summary!=null && shouldRebalanceTrailingStop(summary)){
            log.info("Balancing Started ");
            top20PercentChangeStrategy.execute();
            log.info("Balancing Completed ");
        }

    }

    private boolean shouldRebalance(CryptoTrackingSummary cryptoTrackingSummary){
        BigDecimal pnl = cryptoTrackingSummary.getPnlNonTop20();
        BigDecimal totalCostBasis = cryptoTrackingSummary.getTotalCostBasis();

        if (totalCostBasis.compareTo(BigDecimal.ZERO)==0){
            return false;
        }

        BigDecimal pnlPercentage = pnl.divide(totalCostBasis, 6, RoundingMode.HALF_UP);
        boolean profitThresholdMet = pnlPercentage.compareTo(PNL_THRESHOLD_PERCENT) >= 0;
        boolean stopLossTriggered = pnlPercentage.compareTo(STOP_LOSS_THRESHOLD) <= 0;
        log.info("has Profit threshold met {},has Stop loss threshold  {}",profitThresholdMet,stopLossTriggered);

        boolean shouldRebalance = profitThresholdMet || stopLossTriggered;
        log.info("The result threshold is {}" , shouldRebalance);
        return shouldRebalance;
    }

    private boolean shouldRebalanceTrailingStop(CryptoTrackingSummary cryptoTrackingSummary){
        BigDecimal pnl = cryptoTrackingSummary.getPnlNonTop20();
        BigDecimal totalCostBasis = cryptoTrackingSummary.getTotalCostBasis();
        BigDecimal pnlPercentage = pnl.divide(totalCostBasis, 6, RoundingMode.HALF_UP);

        if(pnlPercentage.compareTo(STOP_LOSS_THRESHOLD) <= 0){
            log.info("Stop loss detected and will be triggered for PNL perecentage{}",pnlPercentage);
            resetQueue();

            return true;
        }

        if (totalCostBasis.compareTo(BigDecimal.ZERO)==0){
            return false;
        }


        if(pnlPercentage.compareTo(EXTREME_SELL_OFF) >=0){
            log.info("Extreme difference detected sell of will be initiated ");
            resetQueue();
            return true;
        }


        if (pnlPercentage.compareTo(PNL_ENTRY_THRESHOLD_PERCENT)<0){
            log.info("PNL below entry threshold ({}). Current Percent {} Resetting queue.", PNL_ENTRY_THRESHOLD_PERCENT,pnlPercentage);
            resetQueue();
            return false;
        }


        if (pnlPercentage.compareTo(PNL_ENTRY_THRESHOLD_PERCENT)>=0){
            pnlQueue.offerLast(pnlPercentage);
            log.info("PNL percentage added to queue: {}. Queue size: {}", pnlPercentage, pnlQueue.size());
        }

        if (!pnlQueue.isEmpty() && pnlQueue.size()>=2) {
            BigDecimal firstEntry = pnlQueue.getFirst();
            BigDecimal diffFromFirst=pnlPercentage.subtract(firstEntry);
            log.info("First Entry  PNL is {} with queue size {}, The difference is {} ",firstEntry,pnlQueue.size(),diffFromFirst);
            if (diffFromFirst.compareTo(LARGE_THRESHOLD)> 0){
                log.info("Large change detected. Re-balancing. Diff: {}, Latest PNL %: {}, First PNL % {}", diffFromFirst, pnlPercentage, firstEntry);
                return true;
            }
        }

        if(pnlQueue.size()>=2){
            List<BigDecimal> queueAsList = new ArrayList<>(pnlQueue);
            BigDecimal secondLastEntry = queueAsList.get(queueAsList.size() - 2);
            BigDecimal diffFromSecondLast=pnlPercentage.subtract(secondLastEntry);
            log.info("the difference from Second last is {}",diffFromSecondLast);
            if (diffFromSecondLast.abs().compareTo(SMALL_THRESHOLD) <= 0) {
                log.info("Small change detected. Continuing.");
                return false;
            }
            if (diffFromSecondLast.compareTo(SMALL_THRESHOLD) > 0
                    && diffFromSecondLast.compareTo(MEDIUM_THRESHOLD) >= 0) {
                log.info("Medium change detected. Selling {}.",diffFromSecondLast);
                return true;
            }
        }else {
            log.info("Not enough entries in queue for second-last comparison. Queue size: {}", pnlQueue.size());
        }
         log.info("No re-balance conditions met. Current PNL %: {}", pnlPercentage);
        return false;
    }


    private void resetQueue(){
        pnlQueue.clear();
    }
}
