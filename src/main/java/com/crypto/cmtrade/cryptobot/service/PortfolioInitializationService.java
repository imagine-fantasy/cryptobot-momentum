package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.BatchTransaction;
import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.crypto.cmtrade.cryptobot.util.OrderSide;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class PortfolioInitializationService  {


    @Autowired
    public DataFetcherService dataFetcherService;

    @Autowired
    public TradeService tradeServices;

    @Autowired
    private CryptoPortfolioService cryptoPortfolioService;

    @Autowired
    private BatchTransactionService service;



    public void initializePortfolioIfNeeded() {

        if(cryptoPortfolioService.isPortfolioEmpty()){
            BigDecimal balance = tradeServices.getAccountBalance();
            BatchTransaction transaction = new BatchTransaction();
            transaction.setStartBalance(balance);
            transaction.setStartTimestamp(LocalDateTime.now());
            BatchTransaction savedTransaction = service.saveBatchTransaction(transaction);
            List<CryptoData> top20 = dataFetcherService.fetchTop20Cryptocurrencies();
            for (CryptoData data : top20){
                BigDecimal result = balance.divide(BigDecimal.valueOf(20), 8, RoundingMode.DOWN);
                tradeServices.executeTrade(savedTransaction.getBatchId(),data.getSymbol(), OrderSide.BUY,result);
            }
            BigDecimal endBalance = tradeServices.getAccountBalance();
            savedTransaction.setEndBalance(endBalance);
            savedTransaction.setEndTimestamp(LocalDateTime.now());
            service.saveBatchTransaction(savedTransaction);



        }

    }


}
