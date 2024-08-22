package com.crypto.cmtrade.cryptobot.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CoinMarketCapResponse {
    @SerializedName("data")
    private List<CryptoData> data;



}
