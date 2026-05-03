export type GoalStatus = 'ACTIVE' | 'COMPLETED' | 'PAUSED' | 'CANCELLED';

export interface Goal {
  id: string;
  name: string;
  targetAmount: number;
  currentAmount: number;
  remaining: number;
  progressPercent: number;
  deadline?: string;
  icon?: string;
  color?: string;
  status: GoalStatus;
  createdAt: string;
}
