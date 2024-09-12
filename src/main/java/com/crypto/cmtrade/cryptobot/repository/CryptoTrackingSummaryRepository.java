package com.crypto.cmtrade.cryptobot.repository;

import com.crypto.cmtrade.cryptobot.model.CryptoTrackingSummary;
import com.crypto.cmtrade.cryptobot.model.PnlSummary;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoTrackingSummaryRepository extends JpaRepository<CryptoTrackingSummary,Long> {

    @NotNull
    public List<CryptoTrackingSummary> findAll();

    @NotNull
    public Optional<CryptoTrackingSummary> findById(@NotNull Long id);

    @Query("select cts from CryptoTrackingSummary cts order by id desc limit 1")
    public CryptoTrackingSummary findMostRecent();

    @NotNull
    public CryptoTrackingSummary save(@NotNull CryptoTrackingSummary cryptoTrackingSummary);


    @Query(value = "SELECT crypto.after_pnl_summary_insert()", nativeQuery = true)
    void callAfterPnlSummaryInsert();

}
