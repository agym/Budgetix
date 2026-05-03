import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface UserProfile {
  currency?: string;
  monthlyIncome?: number;
  financialGoalType?: string;
  timezone?: string;
  notifyBudgetAlerts?: boolean;
  notifyGoalMilestones?: boolean;
  notifyWeeklySummary?: boolean;
  notifyLargeTransactions?: boolean;
}

export interface UserData {
  id: string;
  name: string;
  email: string;
  role: string;
  emailVerified: boolean;
  twoFactorEnabled: boolean;
  profile?: UserProfile;
}

@Injectable({ providedIn: 'root' })
export class UserService extends ApiService {
  constructor(http: HttpClient) { super(http); }

  getMe(): Observable<UserData> {
    return this.get<UserData>('/users/me');
  }

  updateProfile(body: { name: string }): Observable<UserData> {
    return this.put<UserData>('/users/profile', body);
  }

  updateSettings(settings: Partial<UserProfile>): Observable<UserProfile> {
    return this.put<UserProfile>('/users/settings', settings);
  }

  changePassword(body: { currentPassword: string; newPassword: string }): Observable<void> {
    return this.put<void>('/users/password', body);
  }

  enableTwoFactor(enable: boolean): Observable<void> {
    return this.post<void>('/auth/two-factor', { enable });
  }
}
