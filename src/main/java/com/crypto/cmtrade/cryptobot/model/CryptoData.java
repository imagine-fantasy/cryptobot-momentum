package com.crypto.cmtrade.cryptobot.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
@Data
@NoArgsConstructor
public class CryptoData {
    @SerializedName("id")
    private BigInteger id;

    @SerializedName("name")
    private String cryptoCurrency;

    @SerializedName("symbol")
    private String symbol;

    @SerializedName("circulating_supply")
    private BigDecimal balance;

    @SerializedName("market_cap")
    private BigDecimal marketCap;

    @SerializedName("cmc_rank")
    private Integer rank;

    private BigDecimal price;
    private BigDecimal priceChangePercent;
    private BigDecimal volume24h;


    public CryptoData(String symbol, String cryptoCurrency, BigDecimal price, BigDecimal priceChangePercent, BigDecimal volume24h) {
        this.symbol = symbol;
        this.cryptoCurrency = cryptoCurrency;
        this.price = price;
        this.priceChangePercent = priceChangePercent;
        this.volume24h = volume24h;
    }
}

