-- Add transfer pair support to transactions
ALTER TABLE transactions
    ADD COLUMN transfer_pair_id UUID,
    ADD COLUMN transfer_credit  BOOLEAN DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_transactions_transfer_pair ON transactions(transfer_pair_id)
    WHERE transfer_pair_id IS NOT NULL;
