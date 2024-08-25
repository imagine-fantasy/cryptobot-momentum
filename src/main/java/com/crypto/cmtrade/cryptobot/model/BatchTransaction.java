package com.crypto.cmtrade.cryptobot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@Entity
@Table(name = "batch_transactions")
public class BatchTransaction {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "batch_transactions_batch_id_seq")
    @SequenceGenerator(name = "batch_transactions_batch_id_seq", sequenceName = "batch_transactions_batch_id_seq",allocationSize =1)
    private BigInteger batchId;
    private BigDecimal startBalance;
    private BigDecimal endBalance;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
}
