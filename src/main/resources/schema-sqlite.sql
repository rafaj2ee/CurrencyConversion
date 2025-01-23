CREATE TABLE IF NOT EXISTS purchase_transaction (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    description TEXT,
    amount NUMERIC,
    transaction_date DATETIME
);
