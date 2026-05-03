import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Account } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountService extends ApiService {
  constructor(http: HttpClient) { super(http); }

  getAll(): Observable<Account[]> {
    return this.get<Account[]>('/accounts');
  }

  getById(id: string): Observable<Account> {
    return this.get<Account>(`/accounts/${id}`);
  }

  create(req: Partial<Account> & { initialBalance?: number }): Observable<Account> {
    return this.post<Account>('/accounts', req);
  }

  update(id: string, req: Partial<Account>): Observable<Account> {
    return this.put<Account>(`/accounts/${id}`, req);
  }

  remove(id: string): Observable<void> {
    return this.delete<void>(`/accounts/${id}`);
  }
}
