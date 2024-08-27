package com.crypto.cmtrade.cryptobot.util;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

@Getter
@ToString
@FieldNameConstants
public enum TradeStatus {
    ACTIVE("Cryptocurrency is currently held and in the top 20 list"),
    ATTEMPTED_BUY_BELOW_MIN("Buy attempt failed: below minimum quantity"),
    ATTEMPTED_BUY_ABOVE_MAX("Buy attempt failed: above maximum quantity"),
    SELL_COMPLETE("Sell order completed successfully"),
    REMOVED_FROM_TOP20("Removed from portfolio as it's no longer in the top 20"),
    FAILED_MINIMUM_NOTIONAL("Failed to meet minimum notional value for crypto currency");
    private final String reason;

    TradeStatus(String reason) {
        this.reason = reason;
    }


}