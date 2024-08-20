package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.BatchTransaction;
import com.crypto.cmtrade.cryptobot.repository.BatchTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchTransactionService {

    @Autowired
    private BatchTransactionRepository batchTransactionRepository;

    public List<BatchTransaction> getAllBatchTransactions() {
        return batchTransactionRepository.findAll();
    }

    public BatchTransaction getBatchTransactionById(Long id) {
        return batchTransactionRepository.findById(id).orElse(null);
    }

    public BatchTransaction saveBatchTransaction(BatchTransaction batchTransaction) {
        return batchTransactionRepository.save(batchTransaction);
    }

    public void deleteBatchTransaction(Long id) {
        batchTransactionRepository.deleteById(id);
    }
}