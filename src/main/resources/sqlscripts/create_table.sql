CREATE TABLE crypto.crypto_portfolio (
    id BIGSERIAL PRIMARY KEY,
    crypto_currency VARCHAR(50),
    symbol VARCHAR(10) UNIQUE,
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




CREATE TABLE crypto.transaction_log (
    transaction_id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT,
    crypto_currency VARCHAR(50),
    symbol VARCHAR(10),
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