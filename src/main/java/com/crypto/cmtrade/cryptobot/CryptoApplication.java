package com.crypto.cmtrade.cryptobot;

import com.crypto.cmtrade.cryptobot.client.BinanceMarketTickerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptoApplication implements CommandLineRunner {

	@Autowired
	private BinanceMarketTickerClient webSocketClient;

	public static void main(String[] args) {
		SpringApplication.run(CryptoApplication.class, args);
	}

	@Override
	public void run(String... args) {
		webSocketClient.connect();
	}
}
