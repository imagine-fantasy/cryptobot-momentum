package com.crypto.cmtrade.cryptobot.event;

import com.crypto.cmtrade.cryptobot.model.CryptoData;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CryptoDataUpdateEvent extends ApplicationEvent {
    private final Map<String, CryptoData> allCryptoDataMap;
    private final List<CryptoData> top20CryptoData;

    public CryptoDataUpdateEvent(Object source, Map<String, CryptoData> allCryptoDataMap, List<CryptoData> top20CryptoData) {
        super(source);
        this.allCryptoDataMap = allCryptoDataMap;
        this.top20CryptoData = top20CryptoData;
    }


    // Getters...
}