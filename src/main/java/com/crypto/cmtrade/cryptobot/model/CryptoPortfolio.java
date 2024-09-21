package com.crypto.cmtrade.cryptobot.model;

import com.crypto.cmtrade.cryptobot.util.TradeStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "crypto_portfolio")
public class CryptoPortfolio {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crypto_portfolio_id_seq")
    @SequenceGenerator(name = "crypto_portfolio_id_seq", sequenceName = "crypto_portfolio_id_seq",allocationSize =1)
    private BigInteger id;
    private String cryptoCurrency;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal amount;
    private BigDecimal marketCap;
    private Integer rank;
    private BigDecimal lastPrice;
    private BigInteger BatchId;
    @Enumerated(EnumType.STRING)
    private TradeStatus status;
    private String statusReason;
    private LocalDateTime lastUpdated;
    private BigInteger orderId;
    private BigDecimal lastKnownPnl;
    private BigDecimal rollingPctChange24h;
    private LocalDateTime pnlUpdatedAt;



}
