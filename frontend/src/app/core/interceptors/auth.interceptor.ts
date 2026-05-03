import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError, BehaviorSubject } from 'rxjs';
import { catchError, switchMap, filter, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

let isRefreshing = false;
const refreshSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getAccessToken();

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/')) {
        if (!isRefreshing) {
          isRefreshing = true;
          refreshSubject.next(null);

          return auth.refreshTokens().pipe(
            switchMap(tokens => {
              isRefreshing = false;
              refreshSubject.next(tokens.accessToken);
              return next(req.clone({ setHeaders: { Authorization: `Bearer ${tokens.accessToken}` } }));
            }),
            catchError(err => {
              isRefreshing = false;
              auth.logout();
              return throwError(() => err);
            })
          );
        }

        return refreshSubject.pipe(
          filter(t => t !== null),
          take(1),
          switchMap(t => next(req.clone({ setHeaders: { Authorization: `Bearer ${t}` } })))
        );
      }
      return throwError(() => error);
    })
  );
};
