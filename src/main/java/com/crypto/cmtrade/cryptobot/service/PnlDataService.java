package com.crypto.cmtrade.cryptobot.service;

import com.crypto.cmtrade.cryptobot.model.PnlData;
import com.crypto.cmtrade.cryptobot.model.TransactionLog;
import com.crypto.cmtrade.cryptobot.repository.PnlDataRepository;
import com.crypto.cmtrade.cryptobot.repository.TransactionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PnlDataService {

    @Autowired
    private PnlDataRepository pnlDataRepository;

    public List<PnlData> getAllPnlData() {
        return pnlDataRepository.findAll();
    }

    public PnlData getPnlDataById(Long id) {
        return pnlDataRepository.findById(id).orElse(null);
    }

    public PnlData savePnlData(PnlData pnlData) {
        return pnlDataRepository.save(pnlData);
    }

    public void deletePnlDataById(Long id) {
        pnlDataRepository.deleteById(id);
    }
}