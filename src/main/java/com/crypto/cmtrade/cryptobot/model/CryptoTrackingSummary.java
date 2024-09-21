package com.crypto.cmtrade.cryptobot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@Table(name = "crypto_tracking_summary")
@ToString
public class CryptoTrackingSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crypto_tracking_summary_id_seq")
    @SequenceGenerator(name = "crypto_tracking_summary_id_seq", sequenceName = "crypto_tracking_summary_id_seq",allocationSize =1)
    private BigInteger id;

    private BigInteger summaryId;
    private BigDecimal pnlNonTop20;
    private BigDecimal totalCostBasis;
    private LocalDateTime timestamp;
    @Column(name = "non_top20_count")
    private BigInteger nonTop20Count;

    // Getters and setters
}