package com.crypto.cmtrade.cryptobot.repository;

import com.crypto.cmtrade.cryptobot.model.PnlData;
import com.crypto.cmtrade.cryptobot.model.TransactionLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PnlDataRepository extends JpaRepository<PnlData,Long> {

    @NotNull
    public List<PnlData> findAll();

    @NotNull
    public Optional<PnlData> findById(@NotNull Long id);


    @NotNull
    public PnlData save(@NotNull PnlData pnlData);

}
