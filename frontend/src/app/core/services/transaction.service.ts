import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ApiResponse, PageResponse } from '../models/api.model';
import { Transaction, TransactionRequest, TransferRequest } from '../models/transaction.model';
import { map } from 'rxjs/operators';

export interface TransactionFilter {
  startDate?: string;
  endDate?: string;
  categoryId?: string;
  accountId?: string;
  type?: string;
  search?: string;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class TransactionService extends ApiService {
  constructor(http: HttpClient) { super(http); }

  getAll(filter: TransactionFilter = {}): Observable<PageResponse<Transaction>> {
    return this.get<PageResponse<Transaction>>('/transactions', filter as any);
  }

  getById(id: string): Observable<Transaction> {
    return this.get<Transaction>(`/transactions/${id}`);
  }

  create(req: TransactionRequest): Observable<Transaction> {
    return this.post<Transaction>('/transactions', req);
  }

  update(id: string, req: Partial<TransactionRequest>): Observable<Transaction> {
    return this.put<Transaction>(`/transactions/${id}`, req);
  }

  remove(id: string): Observable<void> {
    return this.delete<void>(`/transactions/${id}`);
  }

  bulkDelete(ids: string[]): Observable<void> {
    return this.post<void>('/transactions/bulk-delete', { ids });
  }

  transfer(req: TransferRequest): Observable<Transaction[]> {
    return this.post<Transaction[]>('/transactions/transfer', req);
  }

  importCsv(accountId: string, file: File): Observable<Transaction[]> {
    const form = new FormData();
    form.append('file', file);
    form.append('accountId', accountId);
    return this.http.post<ApiResponse<Transaction[]>>(`${this.base}/transactions/import`, form)
      .pipe(map(r => r.data!));
  }

  uploadReceipt(id: string, file: File): Observable<string> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ApiResponse<string>>(`${this.base}/transactions/${id}/receipt`, form)
      .pipe(map(r => r.data!));
  }
}
