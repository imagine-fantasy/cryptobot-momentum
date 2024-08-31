package com.crypto.cmtrade.cryptobot.config;

import com.crypto.cmtrade.cryptobot.listener.CustomRetryListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
public class RetryConfig {
    // You can add any retry-specific beans or configurations here if needed


    @Bean
    public RetryTemplate retryTemplate(CustomRetryListener customRetryListener) {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.registerListener(customRetryListener);
        return retryTemplate;
    }

}