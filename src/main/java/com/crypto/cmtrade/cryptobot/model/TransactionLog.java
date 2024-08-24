package com.crypto.cmtrade.cryptobot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "transaction_log")
public class TransactionLog {

    @Id
    @GeneratedValue
    private BigInteger transactionId;
    private BigInteger batchId;
    private String cryptoCurrency;
    private String type;
    private BigDecimal price;
    private LocalDateTime timestamp;
}
