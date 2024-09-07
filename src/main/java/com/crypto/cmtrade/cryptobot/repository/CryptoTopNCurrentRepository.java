package com.crypto.cmtrade.cryptobot.repository;

import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import com.crypto.cmtrade.cryptobot.model.CryptoTopNCurrent;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoTopNCurrentRepository extends JpaRepository<CryptoTopNCurrent,Long> {

    @NotNull
    public List<CryptoTopNCurrent> findAll();

    @NotNull
    public Optional<CryptoTopNCurrent> findById(@NotNull Long id);

    @Query("SELECT cp FROM CryptoTopNCurrent  cp WHERE cp.symbol = :symbol")
    public Optional<CryptoTopNCurrent> findBySymbol(@NotNull String symbol);


    @NotNull
    public CryptoTopNCurrent save(@NotNull CryptoTopNCurrent transaction);


    @Modifying
    @Transactional
    void deleteBySymbol(String symbol);

    // If you want to ensure the delete operation was successful and get the number of deleted entries
    @Modifying
    @Transactional
    @Query("DELETE FROM CryptoTopNCurrent  c ")
    int deleteAllFromTopN();


}
