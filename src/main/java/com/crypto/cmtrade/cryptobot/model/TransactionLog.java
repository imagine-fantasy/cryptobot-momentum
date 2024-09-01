package com.crypto.cmtrade.cryptobot.model;


import com.crypto.cmtrade.cryptobot.util.TradeStatus;
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
@Table(name = "transaction_log")
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_log_transaction_id_seq")
    @SequenceGenerator(name = "transaction_log_transaction_id_seq", sequenceName = "transaction_log_transaction_id_seq",allocationSize =1)
    private BigInteger transactionId;
    private BigInteger batchId;
    private String cryptoCurrency;
    private String side;
    private BigDecimal quantity;
    private LocalDateTime timestamp;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    private TradeStatus status;
    private String statusReason;
    private BigInteger orderId;
    private BigDecimal amount;

    private BigDecimal executedAmount;

}
