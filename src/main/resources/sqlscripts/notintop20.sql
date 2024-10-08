--function being called from java code as a function


DROP FUNCTION crypto.after_pnl_summary_insert()




CREATE OR REPLACE FUNCTION crypto.after_pnl_summary_insert()
RETURNS VOID AS $$
DECLARE
    pnl_non_top20 DECIMAL(20,8);
    total_cost_basis DECIMAL(20,8);
    non_top20_portfolio JSONB;
    pnl_summary_snapshot JSONB;
    latest_summary crypto.pnl_summary;
BEGIN
    -- Get the latest pnl_summary record
    SELECT * INTO latest_summary FROM crypto.pnl_summary ORDER BY summary_id DESC LIMIT 1;

    -- Lock the crypto_tracking_summary table
    LOCK TABLE crypto.crypto_tracking_summary IN EXCLUSIVE MODE;

    -- Calculate PNL for non-top20 cryptocurrencies
    SELECT COALESCE(SUM(last_known_pnl), 0) INTO pnl_non_top20
    FROM crypto.crypto_portfolio o
    WHERE symbol NOT IN (
        SELECT crypto_currency FROM crypto.crypto_topn_current
        INTERSECT
        SELECT symbol FROM crypto.crypto_portfolio
    );

    -- Calculate total cost basis
    SELECT COALESCE(SUM(amount), 0) INTO total_cost_basis
    FROM crypto.crypto_portfolio;

    -- Get non-top20 portfolio data
    SELECT JSONB_AGG(JSONB_BUILD_OBJECT(
        'symbol', o.symbol,
        'quantity', o.quantity,
        'amount', o.amount,
        'last_price', o.last_price,
        'last_known_pnl', o.last_known_pnl
    )) INTO non_top20_portfolio
    FROM crypto.crypto_portfolio o
    WHERE symbol NOT IN (
        SELECT crypto_currency FROM crypto.crypto_topn_current
        INTERSECT
        SELECT symbol FROM crypto.crypto_portfolio
    );

    -- Get PNL summary data
    pnl_summary_snapshot := JSONB_BUILD_OBJECT(
        'summary_id', latest_summary.summary_id,
        'total_unrealized_pnl', latest_summary.total_unrealized_pnl,
        'total_realized_pnl', latest_summary.total_realized_pnl,
        'total_current_value', latest_summary.total_current_value,
        'total_cost_basis', latest_summary.total_cost_basis,
        'timestamp', latest_summary.timestamp
    );

    -- Insert into tracking table
    INSERT INTO crypto.crypto_tracking_summary (
        summary_id,
        pnl_non_top20,
        non_top20_portfolio_snapshot,
        pnl_summary_snapshot,
        total_cost_basis
    ) VALUES (
        latest_summary.summary_id,
        pnl_non_top20,
        non_top20_portfolio,
        pnl_summary_snapshot,
        total_cost_basis
    );
END;
$$ LANGUAGE plpgsql;