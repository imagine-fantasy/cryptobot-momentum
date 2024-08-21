package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.BatchTransaction;
import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import com.crypto.cmtrade.cryptobot.repository.BatchTransactionRepository;
import com.crypto.cmtrade.cryptobot.repository.CryptoPortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CryptoPortfolioService {

    @Autowired
    private CryptoPortfolioRepository cryptoPortfolioRepository;

    public List<CryptoPortfolio> getAllCryptoPortfolios() {
        return cryptoPortfolioRepository.findAll();
    }

    public CryptoPortfolio getCryptoPortfolioById(Long id) {
        return cryptoPortfolioRepository.findById(id).orElse(null);
    }

    public CryptoPortfolio saveCryptoPortfolio(CryptoPortfolio cryptoPortfolio) {
        return cryptoPortfolioRepository.save(cryptoPortfolio);
    }

    public void deleteCryptoPortfolio(Long id) {
        cryptoPortfolioRepository.deleteById(id);
    }
}