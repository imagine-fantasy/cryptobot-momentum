package com.crypto.cmtrade.cryptobot.repository;

import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import com.crypto.cmtrade.cryptobot.model.TransactionLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog,Long> {

    @NotNull
    public List<TransactionLog> findAll();

    @NotNull
    public Optional<TransactionLog> findById(@NotNull Long id);


    @NotNull
    public TransactionLog save(@NotNull TransactionLog transaction);

}
