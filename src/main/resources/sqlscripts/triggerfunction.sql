DROP FUNCTION crypto.after_pnl_summary_insert()


-- First, create the function that the trigger will call
CREATE OR REPLACE FUNCTION crypto.after_pnl_summary_update()
RETURNS TRIGGER AS $$
DECLARE
    pnl_non_top20 DECIMAL(20,8);
	total_cost_basis DECIMAL(20,8);
	avg_non_topn_change DECIMAL(20,8);
	avg_topn_change DECIMAL(20,8);
    non_top20_portfolio JSONB;
    pnl_summary_snapshot JSONB;
    non_top20_count INTEGER; -- New variable to store the count
	batch_id_l INTEGER;
	batch_timestamp TIMESTAMP;
BEGIN



	-- Acquire an exclusive lock on the crypto_tracking_summary table
    LOCK TABLE crypto.crypto_tracking_summary IN EXCLUSIVE MODE;

    -- Calculate PNL for non-top20 cryptocurrencies
    SELECT COALESCE(SUM(last_known_pnl), 0), count(1), avg(rolling_pct_change24h)  INTO pnl_non_top20,non_top20_count,avg_non_topn_change
    FROM crypto.crypto_portfolio o
    WHERE symbol NOT IN (
        SELECT crypto_currency FROM crypto.crypto_topn_current
        INTERSECT
        SELECT symbol FROM crypto.crypto_portfolio
    );

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

    ----------------------------------------------------------------
    SELECT avg(rolling_pct_change24h)  into avg_topn_change
    FROM crypto.crypto_topn_current where crypto_currency not in (
    SELECT symbol FROM crypto.crypto_portfolio);

	  -- Calculate total cost basis
    SELECT COALESCE(SUM(cpo.last_price * cpo.quantity), 0) INTO total_cost_basis
    FROM crypto.crypto_portfolio cpo where cpo.quantity is not null ;

    -- Get PNL summary data
    pnl_summary_snapshot := JSONB_BUILD_OBJECT(
        'summary_id', NEW.summary_id,
        'total_unrealized_pnl', NEW.total_unrealized_pnl,
        'total_realized_pnl', NEW.total_realized_pnl,
        'total_current_value', NEW.total_current_value,
        'total_cost_basis', NEW.total_cost_basis,
        'timestamp', NEW.timestamp
    );

	select batch_id,end_timestamp into batch_id_l,batch_timestamp
	from crypto.batch_transactions order by batch_id desc limit 1;

    -- Insert into tracking table
    INSERT INTO crypto.crypto_tracking_summary (
        summary_id,
        pnl_non_top20,
        non_top20_portfolio_snapshot,
        pnl_summary_snapshot,
		total_cost_basis,
		non_top20_count,
		avg_topn_change,
		avg_non_topn_change,
		batch_id,
		batch_timestamp
    ) VALUES (
        NEW.summary_id,
        pnl_non_top20,
        non_top20_portfolio,
        pnl_summary_snapshot,
		total_cost_basis,
		non_top20_count,
		avg_topn_change,
		avg_non_topn_change,
		batch_id_l,
		batch_timestamp
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Now, create the trigger that uses this function
CREATE TRIGGER after_pnl_summary_update
AFTER UPDATE ON crypto.pnl_summary
FOR EACH ROW
EXECUTE FUNCTION crypto.after_pnl_summary_update();
