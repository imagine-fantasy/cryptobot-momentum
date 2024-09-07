package com.crypto.cmtrade.cryptobot.repository;

import com.crypto.cmtrade.cryptobot.model.PnlData;
import com.crypto.cmtrade.cryptobot.model.PnlSummary;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PnlSummaryRepository extends JpaRepository<PnlSummary,Long> {

    @NotNull
    public List<PnlSummary> findAll();

    @NotNull
    public Optional<PnlSummary> findById(@NotNull Long id);


    @NotNull
    public PnlSummary save(@NotNull PnlSummary pnlSummary);

}
