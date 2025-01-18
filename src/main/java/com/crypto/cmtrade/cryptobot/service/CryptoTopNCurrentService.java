package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.CryptoTopNCurrent;
import com.crypto.cmtrade.cryptobot.repository.CryptoTopNCurrentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CryptoTopNCurrentService {


    private final CryptoTopNCurrentRepository cryptoTopNArchiveRepository;

    public List<CryptoTopNCurrent> getAllCryptoPortfolios() {
        return cryptoTopNArchiveRepository.findAll();
    }

    public CryptoTopNCurrent getCryptoPortfolioById(Long id) {
        return cryptoTopNArchiveRepository.findById(id).orElse(null);
    }

    public CryptoTopNCurrent saveCryptoPortfolio(CryptoTopNCurrent cryptoTopNCurrent) {
        return cryptoTopNArchiveRepository.save(cryptoTopNCurrent);
    }

    public void deleteCryptoPortfolio(Long id) {
        cryptoTopNArchiveRepository.deleteById(id);
    }


    public int deleteAllCryptoTopNCurrent(){
        return cryptoTopNArchiveRepository.deleteAllFromTopN();
    }





    public List<CryptoTopNCurrent> saveAllCryptoTopNCurrent(List<CryptoTopNCurrent> cryptoTopNCurrents){
        return cryptoTopNArchiveRepository.saveAll(cryptoTopNCurrents);
    }
}