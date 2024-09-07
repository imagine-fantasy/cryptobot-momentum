package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.PnlData;
import com.crypto.cmtrade.cryptobot.model.PnlSummary;
import com.crypto.cmtrade.cryptobot.repository.PnlDataRepository;
import com.crypto.cmtrade.cryptobot.repository.PnlSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PnlSummaryService {

    @Autowired
    private PnlSummaryRepository pnlSummaryRepository;

    public List<PnlSummary> getAllPnlSummary() {
        return pnlSummaryRepository.findAll();
    }

    public PnlSummary getPnlSummaryById(Long id) {
        return pnlSummaryRepository.findById(id).orElse(null);
    }

    public PnlSummary savePnlSummary(PnlSummary pnlSummary) {
        return pnlSummaryRepository.save(pnlSummary);
    }

    public void deletePnlSummaryById(Long id) {
        pnlSummaryRepository.deleteById(id);
    }
}