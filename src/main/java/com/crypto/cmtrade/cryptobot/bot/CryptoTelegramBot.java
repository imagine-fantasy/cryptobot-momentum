//package com.crypto.cmtrade.cryptobot.bot;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
//import org.telegram.telegrambots.meta.api.objects.Update;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//
//@Component
//@Slf4j
//public class CryptoTelegramBot extends TelegramLongPollingBot {
//
//    @Value("${telegram.bot.username}")
//    private String botUsername;
//
//    @Value("${telegram.bot.token}")
//    private String botToken;
//
//    // Inject your services here
//    // private final YourService yourService;
//
//    // public CryptoTelegramBot(YourService yourService) {
//    //     this.yourService = yourService;
//    // }
//
//    @Override
//    public String getBotUsername() {
//        return botUsername;
//    }
//
//    @Override
//    public String getBotToken() {
//        return botToken;
//    }
//
//    @Override
//    public void onUpdateReceived(Update update) {
//        if (update.hasMessage() && update.getMessage().hasText()) {
//            String messageText = update.getMessage().getText();
//            long chatId = update.getMessage().getChatId();
//            switch (messageText) {
//                case "/start":
//                    sendMessage(chatId, "Welcome to CryptoBot! Use /portfolio, /trade, or /summary to interact.");
//                    break;
//                case "/portfolio":
//                    // Call your portfolio service
//                    // String portfolioInfo = yourPortfolioService.getPortfolioSummary();
//                    sendMessage(chatId, "Your portfolio summary here");
//                    break;
//                case "/trade":
//                    // Implement trade functionality
//                    sendMessage(chatId, "Trading functionality not implemented yet");
//                    break;
//                case "/summary":
//                    // Call your summary service
//                    // String summary = yourSummaryService.getDailySummary();
//                    sendMessage(chatId, "Your daily summary here");
//                    break;
//                default:
//                    sendMessage(chatId, "Unknown command. Use /start to see available commands.");
//            }
//        }
//    }
//
//    private void sendMessage(long chatId, String text) {
//        SendMessage message = new SendMessage();
//        message.setChatId(String.valueOf(chatId));
//        message.setText(text);
//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }
//}