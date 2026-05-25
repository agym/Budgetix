ALTER TABLE accounts
    ADD COLUMN institution_name VARCHAR(100),
    ADD COLUMN last_four       CHAR(4);
