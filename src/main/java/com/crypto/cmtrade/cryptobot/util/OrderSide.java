package com.crypto.cmtrade.cryptobot.util;

public enum OrderSide {
    BUY,
    SELL;

    @Override
    public String toString() {
        return name();
    }
}