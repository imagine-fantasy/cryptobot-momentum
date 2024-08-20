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


@Data
@NoArgsConstructor
@Entity
@Table(name = "batch_transactions")
public class BatchTransaction {


    @Id
    @GeneratedValue
    private BigInteger batchId;
    private BigDecimal startBalance;
    private BigDecimal endBalance;
    private Timestamp startTimestamp;
    private Timestamp endTimestamp;
}
