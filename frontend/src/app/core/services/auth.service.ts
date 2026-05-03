import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap, map, switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.model';
import { LoginRequest, RegisterRequest, TokenResponse, UserAuth } from '../models/auth.model';

const ACCESS_KEY = 'budgetix_access';
const REFRESH_KEY = 'budgetix_refresh';
const USER_KEY = 'budgetix_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = environment.apiUrl;

  readonly currentUser = signal<UserAuth | null>(this.loadUser());
  readonly isAuthenticated = computed(() => !!this.currentUser());

  constructor(private http: HttpClient, private router: Router) {}

  register(req: RegisterRequest): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.api}/auth/register`, req).pipe(
      map(() => undefined)
    );
  }

  login(req: LoginRequest): Observable<TokenResponse> {
    return this.http.post<ApiResponse<TokenResponse>>(`${this.api}/auth/login`, req).pipe(
      map(r => r.data!),
      tap(tokens => this.storeTokens(tokens))
    );
  }

  logout(): void {
    const refresh = this.getRefreshToken();
    if (refresh) {
      this.http.post(`${this.api}/auth/logout`, { refreshToken: refresh }).subscribe();
    }
    this.clearTokens();
    this.router.navigate(['/auth/login']);
  }

  refreshTokens(): Observable<TokenResponse> {
    return this.http.post<ApiResponse<TokenResponse>>(`${this.api}/auth/refresh`, {
      refreshToken: this.getRefreshToken()
    }).pipe(
      map(r => r.data!),
      tap(tokens => this.storeTokens(tokens))
    );
  }

  verifyEmail(email: string, code: string): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.api}/auth/verify-email`, { email, code }).pipe(
      map(() => undefined)
    );
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.api}/auth/forgot-password`, { email }).pipe(
      map(() => undefined)
    );
  }

  resetPassword(email: string, code: string, newPassword: string): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.api}/auth/reset-password`, { email, code, newPassword }).pipe(
      map(() => undefined)
    );
  }

  loginWithOAuth2(accessToken: string, refreshToken: string): Observable<UserAuth> {
    localStorage.setItem(ACCESS_KEY, accessToken);
    localStorage.setItem(REFRESH_KEY, refreshToken);
    return this.http.get<ApiResponse<UserAuth>>(`${this.api}/users/me`).pipe(
      map(r => r.data!),
      tap(user => {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        this.currentUser.set(user);
      })
    );
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_KEY);
  }

  private storeTokens(tokens: TokenResponse): void {
    localStorage.setItem(ACCESS_KEY, tokens.accessToken);
    localStorage.setItem(REFRESH_KEY, tokens.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(tokens.user));
    this.currentUser.set(tokens.user);
  }

  private clearTokens(): void {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
  }

  private loadUser(): UserAuth | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}
