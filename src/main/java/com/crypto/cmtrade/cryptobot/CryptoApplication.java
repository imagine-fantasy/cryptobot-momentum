package com.crypto.cmtrade.cryptobot;


import com.crypto.cmtrade.cryptobot.client.BinanceMarketTickerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
//import org.telegram.telegrambots.meta.TelegramBotsApi;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
//import com.crypto.cmtrade.cryptobot.bot.CryptoTelegramBot;
@SpringBootApplication
@EnableScheduling
public class CryptoApplication implements CommandLineRunner {

//	@Autowired
//	private BinanceMarketTickerClient webSocketClient;

	public static void main(String[] args) {
		SpringApplication.run(CryptoApplication.class, args);
	}

	@Override
	public void run(String... args) {
//		webSocketClient.connect();
	}

//	@Bean
//	public TelegramBotsApi telegramBotsApi(CryptoTelegramBot bot) throws TelegramApiException {
//		TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
//		api.registerBot(bot);
//		return api;
//	}
}
