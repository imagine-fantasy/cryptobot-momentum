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
@Table(name = "pnl_summary")
public class PnlSummary {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pnl_summary_summary_id_seq")
    @SequenceGenerator(name = "pnl_summary_summary_id_seq", sequenceName = "pnl_summary_summary_id_seq",allocationSize =1)
    private BigInteger summaryId;
    private BigDecimal totalUnrealizedPnl;
    private BigDecimal totalRealizedPnl;
    private LocalDateTime timestamp;
    private BigDecimal totalCostBasis;
    private BigDecimal totalCurrentValue;


}
