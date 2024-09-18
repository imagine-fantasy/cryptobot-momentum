package com.crypto.cmtrade.cryptobot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "pnl_data")
public class PnlData {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pnl_data_id_seq")
    @SequenceGenerator(name = "pnl_data_id_seq", sequenceName = "pnl_data_id_seq",allocationSize =1)
    private BigInteger id;
    private String symbol;
    private BigDecimal unrealizedPnl;
    private BigDecimal realizedPnl;
    private LocalDateTime lastUpdated;
    private BigDecimal currentPrice;
    private BigDecimal costBasis;
    private BigInteger summaryId;
    private BigDecimal rollingPctChange24h;

}
