package com.crypto.cmtrade.cryptobot.repository;

import com.crypto.cmtrade.cryptobot.model.BatchTransaction;
import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoPortfolioRepository extends JpaRepository<CryptoPortfolio,Long> {

    @NotNull
    public List<CryptoPortfolio> findAll();

    @NotNull
    public Optional<CryptoPortfolio> findById(@NotNull Long id);


    @NotNull
    public CryptoPortfolio save(@NotNull CryptoPortfolio transaction);

}
