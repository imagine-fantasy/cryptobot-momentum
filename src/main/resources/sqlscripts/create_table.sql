CREATE TABLE crypto.crypto_portfolio (
    id BIGSERIAL PRIMARY KEY,
    cryptocurrency VARCHAR(50) UNIQUE,
    symbol VARCHAR(10),
    balance DECIMAL(18, 8),
    price DECIMAL(18, 8),
    market_cap DECIMAL(24, 8),
    rank INTEGER
) TABLESPACE crypto_ts;


CREATE TABLE crypto.transaction_log (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGSERIAL,
    cryptocurrency VARCHAR(50),
    type VARCHAR(10) CHECK(type IN ('BUY', 'SELL')),
    quantity DECIMAL(18, 8),
    price DECIMAL(18, 8),
    timestamp TIMESTAMP
) TABLESPACE crypto_ts;

CREATE TABLE crypto.batch_transactions (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGSERIAL UNIQUE,
    start_balance DECIMAL(24, 8),
    end_balance DECIMAL(24, 8),
    start_timestamp TIMESTAMP,
    end_timestamp TIMESTAMP
) TABLESPACE crypto_ts;
