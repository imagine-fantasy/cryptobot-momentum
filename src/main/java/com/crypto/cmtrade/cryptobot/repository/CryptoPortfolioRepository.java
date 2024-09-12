package com.crypto.cmtrade.cryptobot.repository;

import com.crypto.cmtrade.cryptobot.model.BatchTransaction;
import com.crypto.cmtrade.cryptobot.model.CryptoPortfolio;
import jakarta.persistence.LockModeType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoPortfolioRepository extends JpaRepository<CryptoPortfolio,Long> {

    @NotNull
    public List<CryptoPortfolio> findAll();

    @NotNull
    public Optional<CryptoPortfolio> findById(@NotNull Long id);

    @Query("SELECT cp FROM CryptoPortfolio cp WHERE cp.symbol = :symbol")
    public Optional<CryptoPortfolio> findBySymbol(@NotNull String symbol);


    @NotNull
    public CryptoPortfolio save(@NotNull CryptoPortfolio transaction);


    @Modifying
    @Transactional
    void deleteBySymbol(String symbol);

    // If you want to ensure the delete operation was successful and get the number of deleted entries
    @Modifying
    @Transactional
    @Query("DELETE FROM CryptoPortfolio c WHERE c.symbol = :symbol")
    int deleteBySymbolCustom(String symbol);



    @Query("SELECT cp FROM CryptoPortfolio cp WHERE cp.symbol NOT IN :symbols")
    List<CryptoPortfolio> findAllBySymbolNotIn(@Param("symbols") List<String> symbols);

    @Query("SELECT cp FROM CryptoPortfolio cp WHERE cp.status ='ACTIVE'")
    public List<CryptoPortfolio> findAllByActiveHoldings();
}
