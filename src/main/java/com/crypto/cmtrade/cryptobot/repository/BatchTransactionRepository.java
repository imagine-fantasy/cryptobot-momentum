package com.crypto.cmtrade.cryptobot.repository;

import com.crypto.cmtrade.cryptobot.model.BatchTransaction;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchTransactionRepository extends JpaRepository<BatchTransaction,Long> {

    @NotNull
    public List<BatchTransaction> findAll();

    @NotNull
    public Optional<BatchTransaction> findById(@NotNull Long id);


    @NotNull
    public BatchTransaction save(@NotNull BatchTransaction transaction);

}
