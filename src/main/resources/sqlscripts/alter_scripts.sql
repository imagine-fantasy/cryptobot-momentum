ALTER TABLE crypto.batch_transactions
ADD COLUMN number_of_buys INTEGER DEFAULT 0;

-- Add column for number of sell transactions
ALTER TABLE crypto.batch_transactions
ADD COLUMN number_of_sells INTEGER DEFAULT 0;


ALTER TABLE crypto.batch_transactions
ADD COLUMN executed_amount NUMERIC(24,8) DEFAULT 0;

-- Add comments to the new columns for clarity
COMMENT ON COLUMN crypto.batch_transactions.number_of_buys IS 'Number of buy transactions in this batch';
COMMENT ON COLUMN crypto.batch_transactions.number_of_sells IS 'Number of sell transactions in this batch';


ALTER TABLE crypto.transaction_log
ADD COLUMN executed_amount NUMERIC(24,8) DEFAULT 0

ALTER TABLE crypto.crypto_portfolio
ADD COLUMN rolling_pct_change24h NUMERIC(24,8);

ALTER TABLE crypto.crypto_topn_archive
ADD COLUMN rolling_pct_change24h NUMERIC(24,8);

ALTER TABLE crypto.pnl_data
ADD COLUMN rolling_pct_change24h NUMERIC(24,8);

ALTER TABLE crypto.crypto_topn_current
ADD COLUMN rolling_pct_change24h NUMERIC(24,8);


ALTER TABLE crypto.crypto_portfolio
ADD COLUMN last_known_pnl NUMERIC(24,8),
ADD COLUMN pnl_updated_at TIMESTAMP;;


DROP TRIGGER IF EXISTS after_pnl_summary_insert ON crypto.pnl_summary;

DROP FUNCTION crypto.after_pnl_summary_insert()