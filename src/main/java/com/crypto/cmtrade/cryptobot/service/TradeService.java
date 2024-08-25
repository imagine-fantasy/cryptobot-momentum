package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.client.BinanceApiClient;
import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import com.crypto.cmtrade.cryptobot.model.TransactionLog;
import com.crypto.cmtrade.cryptobot.util.OrderSide;
import com.crypto.cmtrade.cryptobot.util.TradeStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

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
    public boolean executeTrade(BigInteger batchTransactionId, String symbol, OrderSide side, BigDecimal price){


        log.info("{} order for symbol {} for amount {} will be executed at exchange ",side,symbol,price);
        BinanceApiClient.SymbolInfo symbolInfo = binanceApiClient.getSymbolInfo(symbol);
        BigDecimal adjustedAmount = adjustQuantity(price, symbolInfo);
        if (adjustedAmount.compareTo(symbolInfo.minQty) < 0) {
            log.warn("Order quantity {} is below minimum quantity {} for {}", adjustedAmount, symbolInfo.minQty, symbol);
            saveCyprtoFolioAndTransactionLog(batchTransactionId, symbol, side, price,TradeStatus.ATTEMPTED_BUY_BELOW_MIN);
            return false;
        }

        if (adjustedAmount.compareTo(symbolInfo.maxQty) > 0) {
            log.warn("Order quantity {} is above maximum quantity {} for {}", adjustedAmount, symbolInfo.maxQty, symbol);
            saveCyprtoFolioAndTransactionLog(batchTransactionId, symbol, side, price,TradeStatus.ATTEMPTED_BUY_ABOVE_MAX);
            return false;
        }

        boolean result = binanceApiClient.placeOrder(symbol, adjustedAmount, side);
        log.info("{} order for symbol {} for amount {} is  executed at exchange successfully",side,symbol,price);
        log.info("{} order transaction record database initialized",side);

        if(result){
            TradeStatus status= null;
            if(OrderSide.BUY==side){
                status=TradeStatus.ACTIVE;
                CryptoPortfolio cryptoPortfolio = getPortfolio(batchTransactionId, symbol, price, status);
                cryptoPortfolioService.saveCryptoPortfolio(cryptoPortfolio);


            }else if(OrderSide.SELL==side){
                status=TradeStatus.SELL_COMPLETE;
                cryptoPortfolioService.deleteCryptoPortfoliobySymbolCustom(symbol);
            }
            TransactionLog transactionLog = getTransactionLog(batchTransactionId, symbol, side, price, status);
            transactionLogService.saveTransactionLog(transactionLog);
            log.info("{} order transaction record completed successfully in database ",side);
            return result;
        }

        return false;
    }

    private void saveCyprtoFolioAndTransactionLog(BigInteger batchTransactionId, String symbol, OrderSide side, BigDecimal price,TradeStatus status) {
        CryptoPortfolio cryptoPortfolio = getPortfolio(batchTransactionId, symbol, price, status);
        cryptoPortfolioService.saveCryptoPortfolio(cryptoPortfolio);
        TransactionLog transactionLog = getTransactionLog(batchTransactionId, symbol, side, price, status);
        transactionLogService.saveTransactionLog(transactionLog);
    }

    @NotNull
    private TransactionLog getTransactionLog(BigInteger batchTransactionId, String symbol, OrderSide side, BigDecimal price, TradeStatus status) {
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setPrice(price);
        transactionLog.setType(side.toString());
        transactionLog.setCryptoCurrency(symbol);
        transactionLog.setBatchId(batchTransactionId);
        transactionLog.setTimestamp(LocalDateTime.now());
        transactionLog.setStatus(status);
        transactionLog.setStatusReason(status.getReason());

        return transactionLog;
    }

    @NotNull
    private  CryptoPortfolio getPortfolio(BigInteger batchTransactionId, String symbol, BigDecimal price, TradeStatus status) {
        CryptoPortfolio cryptoPortfolio=new CryptoPortfolio();
        cryptoPortfolio.setCryptoCurrency(symbol);
        cryptoPortfolio.setSymbol(symbol);
        cryptoPortfolio.setBalance(price);
        cryptoPortfolio.setTransactionBatchId(batchTransactionId);
        cryptoPortfolio.setStatus(status);
        cryptoPortfolio.setStatusReason(status.getReason());
        return cryptoPortfolio;
    }

    public BigDecimal getAccountBalance(){
        return binanceApiClient.getAccountBalance();
    }

    private BigDecimal adjustQuantity(BigDecimal quantity, BinanceApiClient.SymbolInfo symbolInfo) {
        BigDecimal remainder = quantity.remainder(symbolInfo.stepSize);
        if (remainder.compareTo(BigDecimal.ZERO) == 0) {
            return quantity;
        }
        return quantity.subtract(remainder);
    }
    @PostConstruct
    public void initialize(){
        binanceApiClient.initializeSymbolInfo();
    }
}
