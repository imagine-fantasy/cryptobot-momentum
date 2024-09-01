package com.crypto.cmtrade.cryptobot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ApplicationRestartService {

    @Autowired
    private ApplicationContext context;

    public void restartApplication() {
        Thread restartThread = new Thread(() -> {
            try {
                Thread.sleep(1000); // Small delay to allow the response to be sent back to the client
                SpringApplication.exit(context, () -> 0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        restartThread.setDaemon(false);
        restartThread.start();
    }
}