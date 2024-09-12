package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.CryptoTrackingSummary;
import com.crypto.cmtrade.cryptobot.model.PnlSummary;
import com.crypto.cmtrade.cryptobot.repository.CryptoTrackingSummaryRepository;
import com.crypto.cmtrade.cryptobot.repository.PnlSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CryptoTrackingSummaryService {

    @Autowired
    private CryptoTrackingSummaryRepository cryptoTrackingSummaryRepository;

    public List<CryptoTrackingSummary> getAllCryptoTrackingSummary() {
        return cryptoTrackingSummaryRepository.findAll();
    }

    public CryptoTrackingSummary getCryptoTrackingSummaryById(Long id) {
        return cryptoTrackingSummaryRepository.findById(id).orElse(null);
    }

    public CryptoTrackingSummary saveCryptoTrackingSummary(CryptoTrackingSummary pnlSummary) {
        return cryptoTrackingSummaryRepository.save(pnlSummary);
    }

    public CryptoTrackingSummary getMostRecent(){
       return cryptoTrackingSummaryRepository.findMostRecent();
    }
    public void deleteCryptoTrackingSummaryById(Long id) {
        cryptoTrackingSummaryRepository.deleteById(id);
    }

    public void callAfterPnlSummaryInsert(){
        cryptoTrackingSummaryRepository.callAfterPnlSummaryInsert();
    };
}