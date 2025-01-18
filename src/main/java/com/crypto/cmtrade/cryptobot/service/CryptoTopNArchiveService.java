package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.CryptoTopNArchive;
import com.crypto.cmtrade.cryptobot.model.CryptoTopNCurrent;
import com.crypto.cmtrade.cryptobot.repository.CryptoTopNArchiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CryptoTopNArchiveService {


    private final CryptoTopNArchiveRepository cryptoTopNArchiveRepository;

    public List<CryptoTopNArchive> getAllCryptoPortfolios() {
        return cryptoTopNArchiveRepository.findAll();
    }

    public CryptoTopNArchive getCryptoPortfolioById(Long id) {
        return cryptoTopNArchiveRepository.findById(id).orElse(null);
    }

    public CryptoTopNArchive saveCryptoPortfolio(CryptoTopNArchive cryptoTopNCurrent) {
        return cryptoTopNArchiveRepository.save(cryptoTopNCurrent);
    }

    public List<CryptoTopNArchive> saveAllCryptoPortfolios(List<CryptoTopNArchive> cryptoTopNArchives){
        return cryptoTopNArchiveRepository.saveAll(cryptoTopNArchives);
    }









}