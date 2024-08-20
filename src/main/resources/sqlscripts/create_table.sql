CREATE TABLE crypto.crypto_portfolio (
    id BIGSERIAL PRIMARY KEY,
    cryptocurrency VARCHAR(50) UNIQUE,
    symbol VARCHAR(10),
    balance DECIMAL(18, 8),
    price DECIMAL(18, 8),
    market_cap DECIMAL(24, 8),
    rank INTEGER,
    transaction_batch_id BIGINT,
    FOREIGN KEY (transaction_batch_id) REFERENCES crypto.batch_transactions (batch_id)
) TABLESPACE crypto_ts;


CREATE TABLE crypto.transaction_log (
    transaction_id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT,
    cryptocurrency VARCHAR(50),
    type VARCHAR(10) CHECK(type IN ('BUY', 'SELL')),
    quantity DECIMAL(18, 8),
    price DECIMAL(18, 8),
    timestamp TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES crypto.batch_transactions (batch_id)
) TABLESPACE crypto_ts;

CREATE TABLE crypto.batch_transactions (
    batch_id BIGSERIAL PRIMARY KEY,
    start_balance DECIMAL(24, 8),
    end_balance DECIMAL(24, 8),
    start_timestamp TIMESTAMP,
    end_timestamp TIMESTAMP
) TABLESPACE crypto_ts;