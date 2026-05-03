import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-oauth2-callback',
  standalone: true,
  template: `
    <div style="display:flex;align-items:center;justify-content:center;height:100vh;
                font-family:'Inter',sans-serif;gap:0.75rem;color:#64748b;font-size:0.95rem;">
      <span style="width:18px;height:18px;border:2.5px solid #2563eb;border-top-color:transparent;
                   border-radius:50%;animation:spin 0.7s linear infinite;display:inline-block"></span>
      Signing you in…
      <style>@keyframes spin{to{transform:rotate(360deg)}}</style>
    </div>
  `
})
export class OAuth2CallbackComponent implements OnInit {
  private route  = inject(ActivatedRoute);
  private auth   = inject(AuthService);
  private router = inject(Router);

  ngOnInit(): void {
    const { accessToken, refreshToken, error } = this.route.snapshot.queryParams;

    if (error || !accessToken || !refreshToken) {
      this.router.navigateByUrl('/auth/login?error=oauth_failed');
      return;
    }

    this.auth.loginWithOAuth2(accessToken, refreshToken).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: () => this.router.navigateByUrl('/auth/login?error=oauth_failed')
    });
  }
}
