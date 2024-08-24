package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.client.BinanceApiClient;
import com.crypto.cmtrade.cryptobot.model.BatchTransaction;
import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import com.crypto.cmtrade.cryptobot.model.TransactionLog;
import com.crypto.cmtrade.cryptobot.util.OrderSide;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@Service
public class TradeService {

    @Autowired
    private BatchTransactionService batchTransactionService;

    @Autowired
    private CryptoPortfolioService cryptoPortfolioService;

    @Autowired
    private TransactionLogService transactionLogService;

    @Autowired
    BinanceApiClient binanceApiClient;

    //    @Scheduled(fixedDelay = 10000)
    public void executeTrade(BigInteger batchTransactionId, String symbol, OrderSide side, BigDecimal price){

        List<BatchTransaction> batchTransactions = batchTransactionService.getAllBatchTransactions();
        for (BatchTransaction batchTransaction : batchTransactions){
            log.info("batch transaction: " + batchTransaction);
        }
        log.info("{} order for symbol {} for amount {} will be executed at exchange ",side,symbol,price);
        boolean result = binanceApiClient.placeOrder(symbol, price, side);
        log.info("{} order for symbol {} for amount {} is  executed at exchange successfully",side,symbol,price);
        log.info("{} order transaction record database initialized",side);
        if(result){
            if(OrderSide.BUY==side){

                CryptoPortfolio cryptoPortfolio = getPortfolio(batchTransactionId, symbol, price);
                cryptoPortfolioService.saveCryptoPortfolio(cryptoPortfolio);

            }else if(OrderSide.SELL==side){
                cryptoPortfolioService.deleteCryptoPortfoliobySymbolCustom(symbol);
            }
            TransactionLog transactionLog = getTransactionLog(batchTransactionId, symbol, side, price);
            transactionLogService.saveTransactionLog(transactionLog);
            log.info("{} order transaction record completed successfully in database ",side);
        }


    }

    @NotNull
    private TransactionLog getTransactionLog(BigInteger batchTransactionId, String symbol, OrderSide side, BigDecimal price) {
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setPrice(price);
        transactionLog.setType(side.toString());
        transactionLog.setCryptoCurrency(symbol);
        transactionLog.setBatchId(batchTransactionId);
        transactionLog.setTimestamp(LocalDateTime.now());
        return transactionLog;
    }

    @NotNull
    private  CryptoPortfolio getPortfolio(BigInteger batchTransactionId, String symbol, BigDecimal price) {
        CryptoPortfolio cryptoPortfolio=new CryptoPortfolio();
        cryptoPortfolio.setCryptoCurrency(symbol);
        cryptoPortfolio.setSymbol(symbol);
        cryptoPortfolio.setBalance(price);
        cryptoPortfolio.setTransactionBatchId(batchTransactionId);
        return cryptoPortfolio;
    }

    public BigDecimal getAccountBalance(){
        return binanceApiClient.getAccountBalance();
    }
}
