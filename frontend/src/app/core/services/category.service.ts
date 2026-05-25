import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Category } from '../models/category.model';

@Injectable({ providedIn: 'root' })
export class CategoryService extends ApiService {
  constructor(http: HttpClient) { super(http); }

  getAll(): Observable<Category[]> {
    return this.get<Category[]>('/categories');
  }

  create(req: any): Observable<Category> {
    return this.post<Category>('/categories', req);
  }

  update(id: string, req: any): Observable<Category> {
    return this.put<Category>(`/categories/${id}`, req);
  }

  remove(id: string): Observable<void> {
    return this.delete<void>(`/categories/${id}`);
  }

  addRule(id: string, keyword: string): Observable<void> {
    return this.post<void>(`/categories/${id}/rules`, { keyword });
  }
}
