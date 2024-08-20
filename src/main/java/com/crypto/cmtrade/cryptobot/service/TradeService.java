package com.crypto.cmtrade.cryptobot.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class TradeService {



    @Scheduled(fixedDelay = 10000)
    public void executeTrade(){
        log.info("starting");

    }
}
