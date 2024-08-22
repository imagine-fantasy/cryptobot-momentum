package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.BatchTransaction;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class TradeService {

    @Autowired
    private BatchTransactionService batchTransactionService;

//    @Scheduled(fixedDelay = 10000)
    public void executeTrade(){

        List<BatchTransaction> batchTransactions = batchTransactionService.getAllBatchTransactions();
        for (BatchTransaction batchTransaction : batchTransactions){
            log.info("batch transaction: " + batchTransaction);
        }

    }
}
