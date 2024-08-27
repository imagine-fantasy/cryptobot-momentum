package com.crypto.cmtrade.cryptobot.statergy;

import com.crypto.cmtrade.cryptobot.model.BatchTransaction;
import com.crypto.cmtrade.cryptobot.model.CryptoData;
import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import com.crypto.cmtrade.cryptobot.model.TransactionLog;
import com.crypto.cmtrade.cryptobot.service.*;
import com.crypto.cmtrade.cryptobot.util.OrderSide;
import com.crypto.cmtrade.cryptobot.util.TradeStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class Top20PercentChangeStrategy implements TradingStrategy{

    @Autowired
    CryptoPortfolioService cryptoPortfolioService;

    @Autowired
    BatchTransactionService service;

    @Autowired
    TradeService tradeService;

    @Autowired
    PortfolioInitializationService portfolioInitializationService;

    @Autowired
    TransactionLogService transactionLogService;

    @Autowired
    DataFetcherService dataFetcherService;

    @Scheduled(fixedDelay = 30 ,timeUnit = TimeUnit.MINUTES)
    public void execute(){
        log.info("Top20PercentChangeStrategy execution started");
        if(!portfolioInitializationService.initializePortfolioIfNeeded()){
            log.info("Top20PercentChangeStrategy execution started, portfolio found Portfolio Initialized ");
            BatchTransaction transaction = new BatchTransaction();
            BigDecimal balance = tradeService.getAccountBalance();
            transaction.setStartBalance(balance);
            transaction.setStartTimestamp(LocalDateTime.now());
            BatchTransaction savedTransaction = service.saveBatchTransaction(transaction);
            List<CryptoData> refreshList = dataFetcherService.fetchTop20Cryptocurrencies();
            List<CryptoPortfolio> storeTop20List = cryptoPortfolioService.getAllCryptoPortfolios();
//            Set<CryptoData> buyList = refreshList.stream().filter(cryptoData -> !storeTop20List.contains(cryptoData.getSymbol())).collect(Collectors.toSet());
            Set<CryptoData> buyList = refreshList.stream()
                    .filter(cryptoData -> storeTop20List.stream()
                            .noneMatch(storedData -> storedData.getSymbol().equals(cryptoData.getSymbol())))
                    .collect(Collectors.toSet());

            List<String> symbols = refreshList.stream().map(CryptoData::getSymbol).collect(Collectors.toList());
            List<CryptoPortfolio> sellList = cryptoPortfolioService.findAllBySymbolNotIn(symbols);

            for(CryptoPortfolio cryptoData:sellList){
                if(cryptoData.getStatus()== TradeStatus.ACTIVE){
                    tradeService.executeTrade(savedTransaction.getBatchId(), cryptoData.getSymbol(), OrderSide.SELL,cryptoData.getAmount(),BigDecimal.ZERO,cryptoData.getQuantity());
                }else{
                    handleNonActivePortfolio(cryptoData);
                }

            }
            BigDecimal balancePostSell = tradeService.getAccountBalance();
            BigDecimal perCryptoBalance = balancePostSell.divide(BigDecimal.valueOf(buyList.size()), 8, RoundingMode.DOWN);
            for(CryptoData buyData:buyList){
                tradeService.executeTrade(savedTransaction.getBatchId(), buyData.getSymbol(), OrderSide.BUY,perCryptoBalance,buyData.getPrice(),null);
            }
            BigDecimal postExecutionBalance = tradeService.getAccountBalance();
            savedTransaction.setEndTimestamp(LocalDateTime.now());
            savedTransaction.setEndBalance(postExecutionBalance);
            service.saveBatchTransaction(savedTransaction);

            log.info("Top20PercentChangeStrategy execution completed successfully ");
        }



    }


    private void handleNonActivePortfolio(CryptoPortfolio portfolio) {
        // Decide how to handle portfolios that are not active
        // For example, you might want to remove them from the database
        cryptoPortfolioService.deleteCryptoPortfoliobySymbolCustom(portfolio.getSymbol());
        TransactionLog transactionLog = tradeService.getTransactionLog(portfolio.getBatchId(), portfolio.getSymbol(), OrderSide.SELL, portfolio.getQuantity(), TradeStatus.REMOVED_FROM_TOP20,portfolio.getLastPrice(), null,portfolio.getAmount());
        transactionLogService.saveTransactionLog(transactionLog);

        // Or update their status
        // portfolio.setStatus(TradeStatus.REMOVED_FROM_TOP20);
        // cryptoPortfolioService.saveCryptoPortfolio(portfolio);
    }


}
