package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.CryptoTrackingSummary;
import com.crypto.cmtrade.cryptobot.statergy.Top20PercentChangeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DynamicRebalanceService {

    private static final BigDecimal PNL_THRESHOLD_PERCENT=new BigDecimal("0.0056");

    @Autowired
    private CryptoTrackingSummaryService cryptoTrackingSummaryService;

    @Autowired
    private Top20PercentChangeStrategy top20PercentChangeStrategy;

//    @Scheduled(fixedDelay =5, timeUnit = TimeUnit.MINUTES)
    public void checkAndRebalance(){
        CryptoTrackingSummary summary=cryptoTrackingSummaryService.getMostRecent();
        log.info(" Crypto Tracking Summary PNL is {}, for Recorded timeStamp {}", summary.getPnlNonTop20(),summary.getTimestamp());
        if(summary!=null && shouldRebalance(summary)){
            log.info(" Rebalancing Started ");
            top20PercentChangeStrategy.execute();
            log.info(" Rebalancing Completed ");
        }

    }

    private boolean shouldRebalance(CryptoTrackingSummary cryptoTrackingSummary){
        BigDecimal pnl = cryptoTrackingSummary.getPnlNonTop20();
        BigDecimal totalCostBasis = cryptoTrackingSummary.getTotalCostBasis();

        if (totalCostBasis.compareTo(BigDecimal.ZERO)==0){
            return false;
        }

        BigDecimal pnlPercentage = pnl.divide(totalCostBasis, 6, RoundingMode.HALF_UP);
        boolean resultThershold = pnlPercentage.compareTo(PNL_THRESHOLD_PERCENT) >= 0;

        log.info(" The result threshold is {}" , resultThershold);
        return resultThershold;
    }

}
