import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface DashboardOverview {
  totalIncome: number;
  totalExpenses: number;
  netSavings: number;
  netWorth: number;
  savingsRate: number;
  expenseChangePercent: number;
  incomeChangePercent: number;
  avgDailySpend: number;
  avgDailyIncome: number;
  projectedMonthlyExpense: number;
  projectedMonthlyIncome: number;
  projectedEndBalance: number;
  daysRemainingInMonth: number;
  daysUntilZero?: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService extends ApiService {
  constructor(http: HttpClient) { super(http); }

  getOverview(): Observable<DashboardOverview> {
    return this.get<DashboardOverview>('/dashboard/overview');
  }

  getSpendingByCategory(from: string, to: string): Observable<any[]> {
    return this.get<any[]>('/dashboard/charts/spending-by-category', { from, to });
  }

  getIncomeVsExpenses(months = 6): Observable<any[]> {
    return this.get<any[]>('/dashboard/charts/income-vs-expenses', { months });
  }

  getDailyTrend(month: number, year: number): Observable<any[]> {
    return this.get<any[]>('/dashboard/charts/daily-trend', { month, year });
  }
}
