export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';

export interface Transaction {
  id: string;
  amount: number;
  type: TransactionType;
  description?: string;
  notes?: string;
  date: string;
  receipt?: string;
  recurring: boolean;
  tags: string[];
  account: { id: string; name: string; icon?: string; color?: string };
  category?: { id: string; name: string; icon?: string; color?: string };
  transferPairId?: string;
  transferCredit?: boolean;
  createdAt: string;
}

export interface TransferRequest {
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  date: string;
  description?: string;
}

export interface TransactionRequest {
  amount: number;
  type: TransactionType;
  accountId: string;
  categoryId?: string;
  description?: string;
  notes?: string;
  date: string;
  tags?: string[];
}
