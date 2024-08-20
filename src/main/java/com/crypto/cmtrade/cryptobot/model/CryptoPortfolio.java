package com.crypto.cmtrade.cryptobot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@Entity
@Table(name = "crypto_portfolios")
public class CryptoPortfolio {


    @Id
    @GeneratedValue
    private BigInteger id;
    private String cryptoCurrency;
    private String symbol;
    private BigDecimal balance;
    private BigDecimal marketCap;
    private Integer rank;
    private BigInteger transactionBatchId;



}
