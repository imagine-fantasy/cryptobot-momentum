package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.client.BinanceApiClient;
import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import com.crypto.cmtrade.cryptobot.model.OrderResponse;
import com.crypto.cmtrade.cryptobot.model.TransactionLog;
import com.crypto.cmtrade.cryptobot.util.OrderSide;
import com.crypto.cmtrade.cryptobot.util.TradeStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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


    public OrderResponse executeTrade(BigInteger batchTransactionId, String symbol, OrderSide side, BigDecimal amount, BigDecimal price, BigDecimal quantity){

        log.info("{} order for symbol {} for amount {} will be executed at exchange ",side,symbol,amount);
        BinanceApiClient.SymbolInfo symbolInfo = binanceApiClient.getSymbolInfo(symbol);
        OrderResponse result= null;


        TradeStatus status=null;

        BigDecimal adjustedAmount = adjustQuantityForNotional(amount, symbolInfo);

        if (OrderSide.SELL !=side){
            // CHANGE: Check if adjustment was possible

            if (adjustedAmount.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("Cannot meet minimum notional for {} with allocated amount {}. Skipping trade.", symbol, adjustedAmount);
                saveCryptoFolioAndTransactionLog(batchTransactionId, symbol, side, adjustedAmount, TradeStatus.FAILED_MINIMUM_NOTIONAL, price,adjustedAmount);
                return null;
            }

            if (adjustedAmount.compareTo(symbolInfo.minQty) < 0) {
                log.warn("Order quantity {} is below minimum quantity {} for {}", adjustedAmount, symbolInfo.minQty, symbol);
                saveCryptoFolioAndTransactionLog(batchTransactionId, symbol, side, adjustedAmount, TradeStatus.ATTEMPTED_BUY_BELOW_MIN, price,adjustedAmount);
                return null;
            }

            if (adjustedAmount.compareTo(symbolInfo.maxQty) > 0) {
                log.warn("Order quantity {} is above maximum quantity {} for {}", adjustedAmount, symbolInfo.maxQty, symbol);
                saveCryptoFolioAndTransactionLog(batchTransactionId, symbol, side, adjustedAmount, TradeStatus.ATTEMPTED_BUY_ABOVE_MAX, price,adjustedAmount);
                return null;
            }
            try{
                result = binanceApiClient.placeBuyOrder(symbol, adjustedAmount, side);
                status = TradeStatus.ACTIVE;
            }catch(Exception e){
                if ( e.getMessage().contains("Order book liquidity is less than LOT_SIZE filter minimum quantity")) {
                    log.warn("Insufficient liquidity to meet minimum LOT_SIZE for {} on {}. Minimum quantity: {}",
                            side, symbol, symbolInfo.getMinQty());
                    status=TradeStatus.INSUFFICIENT_LIQUIDITY_FOR_LOT_SIZE;
                } else {
                    log.error("Error executing trade: {}", e.getMessage());
                    status=TradeStatus.FAILED;
                }
            }

            log.info("{} order for symbol {} for amount {} is  executed at exchange successfully", side, symbol, adjustedAmount);
            log.info("{} order transaction record database initialized", side);

            CryptoPortfolio cryptoPortfolio = getPortfolio(batchTransactionId, symbol, adjustedAmount, status, price, result);
            cryptoPortfolioService.saveCryptoPortfolio(cryptoPortfolio);


        }else{

            try {
                BigDecimal precisionQuantity = adjustQuantityPrecision(symbolInfo, quantity);
                log.info("{} order for symbol {} for quantity  {} is  will be executed ",side,symbol,precisionQuantity);
                result = binanceApiClient.placeSellOrder(symbol, precisionQuantity, side);
                log.info("{} order for symbol {} for quantity {} is  executed at exchange successfully",side,symbol,precisionQuantity);
                log.info("{} order transaction record database initialized",side);
                status=TradeStatus.SELL_COMPLETE;
                cryptoPortfolioService.deleteCryptoPortfoliobySymbolCustom(symbol);
            } catch (Exception e) {
                status=TradeStatus.FAILED;
                log.error("Sell failed for reason {}", e.getMessage());
                e.printStackTrace();
            }
        }

        TransactionLog transactionLog = getTransactionLog(batchTransactionId, symbol, side, adjustedAmount, status,price,result,adjustedAmount);
        transactionLogService.saveTransactionLog(transactionLog);
        log.info("{} order transaction record completed successfully in database ",side);

        return  result;



    }

    private BigDecimal adjustQuantityPrecision( BinanceApiClient.SymbolInfo symbolInfo, BigDecimal quantity) {
        int stepSize = symbolInfo.getStepSize().stripTrailingZeros().scale();
        return quantity.setScale(stepSize, RoundingMode.DOWN);
    }

    private BigDecimal adjustQuantityForNotional(BigDecimal amount, BinanceApiClient.SymbolInfo symbolInfo) {
        if (amount!=null && amount.compareTo(symbolInfo.getMinNotional()) >= 0) {
            // If we already meet the minimum notional, just adjust for step size
            return amount;
        }

        // Calculate the minimum quantity needed to meet notional

        return BigDecimal.ZERO;
    }
    private void saveCryptoFolioAndTransactionLog(BigInteger batchTransactionId, String symbol, OrderSide side, BigDecimal quantity,TradeStatus status, BigDecimal price,
                                                  BigDecimal adjustedAmount) {
        CryptoPortfolio cryptoPortfolio = getPortfolio(batchTransactionId, symbol, quantity, status,price, null);
        cryptoPortfolioService.saveCryptoPortfolio(cryptoPortfolio);
        TransactionLog transactionLog = getTransactionLog(batchTransactionId, symbol, side, quantity, status,price, null,adjustedAmount);
        transactionLogService.saveTransactionLog(transactionLog);
    }

    @NotNull
    public TransactionLog getTransactionLog(BigInteger batchTransactionId, String symbol, OrderSide side, BigDecimal quantity, TradeStatus status,BigDecimal price,
                                            OrderResponse result,BigDecimal amount) {
        TransactionLog transactionLog = new TransactionLog();

        transactionLog.setSide(side.toString());
        transactionLog.setCryptoCurrency(symbol);
        transactionLog.setBatchId(batchTransactionId);
        transactionLog.setPrice(price);
        transactionLog.setAmount(amount);
        transactionLog.setTimestamp(LocalDateTime.now());
        transactionLog.setStatus(status);
        if(result != null){
            transactionLog.setOrderId(BigInteger.valueOf(result.getOrderId()));
            transactionLog.setQuantity(new BigDecimal(result.getExecutedQty()));
            BigDecimal actualAmount = result.getCummulativeQuoteQty() != null ? new BigDecimal(result.getCummulativeQuoteQty()) : BigDecimal.ZERO;
            transactionLog.setExecutedAmount(actualAmount);
        }
        transactionLog.setStatusReason(status.getReason());

        return transactionLog;
    }

    @NotNull
    private  CryptoPortfolio  getPortfolio(BigInteger batchTransactionId, String symbol, BigDecimal amount, TradeStatus status, BigDecimal price, OrderResponse result) {
        CryptoPortfolio cryptoPortfolio=new CryptoPortfolio();
        cryptoPortfolio.setCryptoCurrency(symbol);
        cryptoPortfolio.setSymbol(symbol);
        cryptoPortfolio.setAmount(amount);
        cryptoPortfolio.setLastPrice(price);
        cryptoPortfolio.setBatchId(batchTransactionId);
        cryptoPortfolio.setStatus(status);
        cryptoPortfolio.setLastUpdated(LocalDateTime.now());
        cryptoPortfolio.setStatusReason(status.getReason());
        if (result !=null){
            cryptoPortfolio.setQuantity(new BigDecimal(result.getExecutedQty()));
            cryptoPortfolio.setOrderId(BigInteger.valueOf(result.getOrderId()));

        }

        return cryptoPortfolio;
    }

    public BigDecimal getAccountBalance(){
        return binanceApiClient.getAccountBalance();
    }

    private BigDecimal adjustQuantity(BigDecimal quantity, BinanceApiClient.SymbolInfo symbolInfo) {
        // Need to understand the below logic

        return quantity.divide(symbolInfo.stepSize, 0, RoundingMode.DOWN).multiply(symbolInfo.stepSize);
    }
    @PostConstruct
    public void initialize(){
        binanceApiClient.initializeSymbolInfo();
    }
}
