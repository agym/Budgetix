import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { PageResponse } from '../models/api.model';

export interface Notification {
  id: string;
  title: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService extends ApiService {
  constructor(http: HttpClient) { super(http); }

  getAll(unreadOnly = false, page = 0, size = 20): Observable<PageResponse<Notification>> {
    return this.get<PageResponse<Notification>>('/notifications', { unreadOnly, page, size });
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.get<{ count: number }>('/notifications/unread-count');
  }

  markRead(id: string): Observable<void> {
    return this.put<void>(`/notifications/${id}/read`, {});
  }

  markAllRead(): Observable<void> {
    return this.put<void>('/notifications/read-all', {});
  }

  remove(id: string): Observable<void> {
    return this.delete<void>(`/notifications/${id}`);
  }
}
