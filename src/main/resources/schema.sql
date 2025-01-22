CREATE TABLE IF NOT EXISTS purchase_transaction (
    id SERIAL PRIMARY KEY,
    description VARCHAR(50) NOT NULL,
    amount NUMERIC NOT NULL,
    transaction_date TIMESTAMP NOT NULL
);
