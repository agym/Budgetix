import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Budget } from '../models/budget.model';

@Injectable({ providedIn: 'root' })
export class BudgetService extends ApiService {
  constructor(http: HttpClient) { super(http); }

  getByPeriod(month: number, year: number): Observable<Budget[]> {
    return this.get<Budget[]>('/budgets', { month, year });
  }

  create(req: any): Observable<Budget> {
    return this.post<Budget>('/budgets', req);
  }

  update(id: string, req: any): Observable<Budget> {
    return this.put<Budget>(`/budgets/${id}`, req);
  }

  remove(id: string): Observable<void> {
    return this.delete<void>(`/budgets/${id}`);
  }
}
