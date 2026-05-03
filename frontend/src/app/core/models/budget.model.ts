export interface Budget {
  id: string;
  amount: number;
  spent: number;
  remaining: number;
  usagePercent: number;
  month: number;
  year: number;
  rollover: boolean;
  rolledAmount: number;
  category?: { id: string; name: string; icon?: string; color?: string };
}
