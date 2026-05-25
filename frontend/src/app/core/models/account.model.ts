export type AccountType = 'CASH' | 'BANK' | 'CREDIT_CARD' | 'SAVINGS' | 'INVESTMENT';

export interface Account {
  id: string;
  name: string;
  type: AccountType;
  balance: number;
  currency: string;
  color?: string;
  icon?: string;
  isDefault: boolean;
  institutionName?: string;
  lastFour?: string;
  createdAt: string;
}
