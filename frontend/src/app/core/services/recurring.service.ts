import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface RecurringTransaction {
  id: string;
  amount: number;
  type: 'INCOME' | 'EXPENSE' | 'TRANSFER';
  description: string;
  frequency: 'DAILY' | 'WEEKLY' | 'BIWEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
  startDate: string;
  endDate?: string;
  nextRun?: string;
  lastRun?: string;
  active: boolean;
  accountName?: string;
  categoryName?: string;
}

export interface RecurringRequest {
  amount: number;
  type: string;
  accountId: string;
  categoryId?: string;
  description: string;
  frequency: string;
  startDate: string;
  endDate?: string;
}

@Injectable({ providedIn: 'root' })
export class RecurringService extends ApiService {
  constructor(http: HttpClient) { super(http); }

  getAll(): Observable<RecurringTransaction[]> {
    return this.get<RecurringTransaction[]>('/recurring');
  }

  create(req: RecurringRequest): Observable<RecurringTransaction> {
    return this.post<RecurringTransaction>('/recurring', req);
  }

  toggle(id: string): Observable<void> {
    return this.post<void>(`/recurring/${id}/toggle`, {});
  }

  remove(id: string): Observable<void> {
    return this.delete<void>(`/recurring/${id}`);
  }
}
