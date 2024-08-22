package com.crypto.cmtrade.cryptobot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-secret.properties")
public class ApiConfig {

    @Value("${coinmarketcap.api.key}")
    private String coinMarketCapApiKey;

    @Value("${exchange.api.key}")
    private String exchangeApiKey;


}