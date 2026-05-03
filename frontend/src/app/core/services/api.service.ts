import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  protected readonly base = environment.apiUrl;

  constructor(protected http: HttpClient) {}

  protected get<T>(path: string, params?: Record<string, any>): Observable<T> {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([k, v]) => {
        if (v !== null && v !== undefined) httpParams = httpParams.set(k, v);
      });
    }
    return this.http.get<ApiResponse<T>>(`${this.base}${path}`, { params: httpParams })
      .pipe(map(r => r.data!));
  }

  protected post<T>(path: string, body: any): Observable<T> {
    return this.http.post<ApiResponse<T>>(`${this.base}${path}`, body)
      .pipe(map(r => r.data!));
  }

  protected put<T>(path: string, body: any): Observable<T> {
    return this.http.put<ApiResponse<T>>(`${this.base}${path}`, body)
      .pipe(map(r => r.data!));
  }

  protected patch<T>(path: string, body: any): Observable<T> {
    return this.http.patch<ApiResponse<T>>(`${this.base}${path}`, body)
      .pipe(map(r => r.data!));
  }

  protected delete<T>(path: string): Observable<T> {
    return this.http.delete<ApiResponse<T>>(`${this.base}${path}`)
      .pipe(map(r => r.data!));
  }

  protected getBlob(path: string, params?: Record<string, any>): Observable<Blob> {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([k, v]) => {
        if (v !== null && v !== undefined) httpParams = httpParams.set(k, v);
      });
    }
    return this.http.get(`${this.base}${path}`, { params: httpParams, responseType: 'blob' });
  }
}
