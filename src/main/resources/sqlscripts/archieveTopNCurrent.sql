CREATE OR REPLACE FUNCTION crypto.archive_crypto_topn_current()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO crypto.crypto_topn_archive (
        id, crypto_currency, symbol, quantity, amount, market_cap, rank, last_price, last_updated,rolling_pct_change24h
    )
    VALUES (
        OLD.id, OLD.crypto_currency, OLD.symbol, OLD.quantity, OLD.amount, OLD.market_cap, OLD.rank, OLD.last_price, OLD.last_updated, OLD.rolling_pct_change24h
    );
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;