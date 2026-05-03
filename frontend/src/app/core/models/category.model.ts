export type CategoryType = 'INCOME' | 'EXPENSE' | 'TRANSFER';

export interface Category {
  id: string;
  name: string;
  icon?: string;
  color?: string;
  type: CategoryType;
  system: boolean;
  parentId?: string;
  children: Category[];
}
