package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import com.crypto.cmtrade.cryptobot.model.CryptoTopNCurrent;
import com.crypto.cmtrade.cryptobot.repository.CryptoPortfolioRepository;
import com.crypto.cmtrade.cryptobot.repository.CryptoTopNCurrentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CryptoTopNCurrentService {

    @Autowired
    private CryptoTopNCurrentRepository cryptoTopNCurrentRepository;

    public List<CryptoTopNCurrent> getAllCryptoPortfolios() {
        return cryptoTopNCurrentRepository.findAll();
    }

    public CryptoTopNCurrent getCryptoPortfolioById(Long id) {
        return cryptoTopNCurrentRepository.findById(id).orElse(null);
    }

    public CryptoTopNCurrent saveCryptoPortfolio(CryptoTopNCurrent cryptoTopNCurrent) {
        return cryptoTopNCurrentRepository.save(cryptoTopNCurrent);
    }

    public void deleteCryptoPortfolio(Long id) {
        cryptoTopNCurrentRepository.deleteById(id);
    }


    public int deleteAllCryptoTopNCurrent(){
        return cryptoTopNCurrentRepository.deleteAllFromTopN();
    }


    public Optional<CryptoTopNCurrent> findBySymbol(String symbol){
        return cryptoTopNCurrentRepository.findBySymbol(symbol);
    }

}