CREATE TABLE crypto.crypto_portfolio (
    id BIGSERIAL PRIMARY KEY,
    crypto_currency VARCHAR(50),
    symbol VARCHAR(30) UNIQUE,
    quantity DECIMAL(18, 8),
    amount DECIMAL(18, 8),
    last_price DECIMAL(18, 8),
    market_cap DECIMAL(24, 8),
    rank INTEGER,
    last_updated TIMESTAMP,
    status VARCHAR(50) CHECK (status IN ('ACTIVE', 'ATTEMPTED_BUY_BELOW_MIN', 'ATTEMPTED_BUY_ABOVE_MAX', 'SELL_COMPLETE', 'REMOVED_FROM_TOP20', 'FAILED_MINIMUM_NOTIONAL')),
    status_reason VARCHAR(255),
    order_id BIGINT,
    batch_id BIGINT,
    FOREIGN KEY (batch_id) REFERENCES crypto.batch_transactions (batch_id)
) TABLESPACE crypto_ts;


CREATE TABLE IF NOT EXISTS crypto.pnl_summary (
    summary_id BIGSERIAL PRIMARY KEY,
    total_unrealized_pnl NUMERIC(24,8),
    total_realized_pnl NUMERIC(24,8),
    total_current_value NUMERIC(24,8),
    total_cost_basis NUMERIC(24,8),
    number_of_positions INTEGER,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE crypto.transaction_log (
    transaction_id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT,
    crypto_currency VARCHAR(50),
    symbol VARCHAR(30),
    side VARCHAR(4) CHECK(side IN ('BUY', 'SELL')),
    quantity DECIMAL(18, 8),
    amount DECIMAL(18, 8),
    price DECIMAL(18, 8),
    timestamp TIMESTAMP,
    status VARCHAR(50) CHECK (status IN ('ACTIVE', 'ATTEMPTED_BUY_BELOW_MIN', 'ATTEMPTED_BUY_ABOVE_MAX', 'SELL_COMPLETE', 'REMOVED_FROM_TOP20', 'FAILED_MINIMUM_NOTIONAL')),
    status_reason VARCHAR(255),
    order_id BIGINT,
    FOREIGN KEY (batch_id) REFERENCES crypto.batch_transactions (batch_id)
) TABLESPACE crypto_ts;


ALTER TABLE crypto.crypto_portfolio
DROP CONSTRAINT IF EXISTS crypto_portfolio_status_check,
ADD CONSTRAINT crypto_portfolio_status_check
CHECK (status IN ('ACTIVE', 'ATTEMPTED_BUY_BELOW_MIN', 'ATTEMPTED_BUY_ABOVE_MAX', 'SELL_COMPLETE', 'REMOVED_FROM_TOP20', 'FAILED_MINIMUM_NOTIONAL', 'INSUFFICIENT_LIQUIDITY_FOR_LOT_SIZE', 'FAILED'));

ALTER TABLE crypto.transaction_log
DROP CONSTRAINT IF EXISTS transaction_log_status_check,
ADD CONSTRAINT transaction_log_status_check
CHECK (status IN ('ACTIVE', 'ATTEMPTED_BUY_BELOW_MIN', 'ATTEMPTED_BUY_ABOVE_MAX', 'SELL_COMPLETE', 'REMOVED_FROM_TOP20', 'FAILED_MINIMUM_NOTIONAL', 'INSUFFICIENT_LIQUIDITY_FOR_LOT_SIZE', 'FAILED'));


CREATE TABLE IF NOT EXISTS crypto.batch_transactions
(
    batch_id bigint NOT NULL DEFAULT nextval('crypto.batch_transactions_batch_id_seq'::regclass),
    start_balance numeric(24,8),
    end_balance numeric(24,8),
    start_timestamp timestamp without time zone,
    end_timestamp timestamp without time zone,
    CONSTRAINT batch_transactions_pkey PRIMARY KEY (batch_id)
) TABLESPACE crypto_ts;
ALTER TABLE IF EXISTS crypto.batch_transactions
    OWNER to postgres;