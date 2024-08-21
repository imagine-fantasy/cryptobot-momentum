package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.BatchTransaction;
import com.crypto.cmtrade.cryptobot.model.TransactionLog;
import com.crypto.cmtrade.cryptobot.repository.BatchTransactionRepository;
import com.crypto.cmtrade.cryptobot.repository.TransactionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionLogService {

    @Autowired
    private TransactionLogRepository transactionLogRepository;

    public List<TransactionLog> getAllTransactionLogs() {
        return transactionLogRepository.findAll();
    }

    public TransactionLog getTransactionLogById(Long id) {
        return transactionLogRepository.findById(id).orElse(null);
    }

    public TransactionLog saveTransactionLog(TransactionLog transactionLog) {
        return transactionLogRepository.save(transactionLog);
    }

    public void deleteTransactionLog(Long id) {
        transactionLogRepository.deleteById(id);
    }
}