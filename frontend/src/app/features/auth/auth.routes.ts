import { Routes } from '@angular/router';
import { AuthShellComponent } from './auth-shell/auth-shell.component';

export const AUTH_ROUTES: Routes = [
  {
    path: 'oauth2/callback',
    loadComponent: () => import('./oauth2-callback/oauth2-callback.component').then(m => m.OAuth2CallbackComponent)
  },
  {
    path: '',
    component: AuthShellComponent,
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      {
        path: 'login',
        loadComponent: () => import('./login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./register/register.component').then(m => m.RegisterComponent)
      },
      {
        path: 'verify-email',
        loadComponent: () => import('./verify-email/verify-email.component').then(m => m.VerifyEmailComponent)
      },
      {
        path: 'forgot-password',
        loadComponent: () => import('./forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
      },
      {
        path: 'reset-password',
        loadComponent: () => import('./reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
      }
    ]
  }
];
