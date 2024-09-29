package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.BatchTransaction;
import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.crypto.cmtrade.cryptobot.util.OrderSide;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PortfolioInitializationService  {


    @Autowired
    public DataFetcherService dataFetcherService;

    @Autowired
    public TradeService tradeServices;

    @Autowired
    private CryptoPortfolioService cryptoPortfolioService;

    @Autowired
    private BatchTransactionService service;



    public boolean initializePortfolioIfNeeded(Map<String, CryptoData> allData, List<CryptoData> top20) {

        if(cryptoPortfolioService.isPortfolioEmpty()){
            log.info("Portfolio is empty, First Purchase will be executed");
            BigDecimal balance = tradeServices.getAccountBalance();
            BatchTransaction transaction = new BatchTransaction();
            transaction.setStartBalance(balance);
            transaction.setStartTimestamp(LocalDateTime.now());
            BatchTransaction savedTransaction = service.saveBatchTransaction(transaction);
            BigDecimal result = balance.divide(BigDecimal.valueOf(top20.size()), 8, RoundingMode.DOWN);
            for (CryptoData data : top20){

                tradeServices.executeTrade(savedTransaction.getBatchId(),data.getSymbol(), OrderSide.BUY,result,data.getPrice(),null);
            }
            BigDecimal endBalance = tradeServices.getAccountBalance();
            savedTransaction.setEndBalance(endBalance);
            savedTransaction.setEndTimestamp(LocalDateTime.now());
            service.saveBatchTransaction(savedTransaction);
            log.info("Portfolio purchase is completed successfully");
            return true;
        }
        return  false;

    }


}
