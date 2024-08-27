package com.crypto.cmtrade.cryptobot.model;

import lombok.Data;

@Data
public class OrderResponse {
    private String symbol;
    private Long orderId;
    private String clientOrderId;
    private String transactTime;
    private String price;
    private String origQty;
    private String executedQty;
    private String cummulativeQuoteQty;
    private String status;
    private String type;
    private String side;

    // Getters and setters
}