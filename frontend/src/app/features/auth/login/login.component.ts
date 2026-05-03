import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../../core/services/auth.service';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, InputTextModule, ButtonModule, TranslatePipe],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  loading        = signal(false);
  error          = signal<string | null>(null);
  needsTwoFactor = signal(false);
  showPw         = signal(false);

  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    email:         ['', [Validators.required, Validators.email]],
    password:      ['', Validators.required],
    twoFactorCode: ['']
  });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.error.set(null);
    const { email, password, twoFactorCode } = this.form.value;
    this.auth.login({ email: email!, password: password!, totpCode: twoFactorCode || undefined }).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: (err) => {
        const msg: string = err?.error?.message ?? '';
        if (msg.includes('2FA') || msg.includes('two-factor')) {
          this.needsTwoFactor.set(true);
          this.error.set('AUTH.LOGIN.TWO_FACTOR_MSG');
        } else if (msg.includes('verify') || msg.includes('email')) {
          this.error.set('AUTH.LOGIN.EMAIL_UNVERIFIED');
        } else {
          this.error.set('AUTH.LOGIN.INVALID_CREDS');
        }
        this.loading.set(false);
      }
    });
  }
}
