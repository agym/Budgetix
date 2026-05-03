import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Goal, GoalStatus } from '../models/goal.model';

@Injectable({ providedIn: 'root' })
export class GoalService extends ApiService {
  constructor(http: HttpClient) { super(http); }

  getAll(): Observable<Goal[]> {
    return this.get<Goal[]>('/goals');
  }

  create(req: any): Observable<Goal> {
    return this.post<Goal>('/goals', req);
  }

  update(id: string, req: any): Observable<Goal> {
    return this.put<Goal>(`/goals/${id}`, req);
  }

  contribute(id: string, amount: number, note?: string): Observable<Goal> {
    return this.post<Goal>(`/goals/${id}/contribute`, { amount, note });
  }

  updateStatus(id: string, status: GoalStatus): Observable<void> {
    return this.patch<void>(`/goals/${id}/status`, { status });
  }

  remove(id: string): Observable<void> {
    return this.delete<void>(`/goals/${id}`);
  }
}
