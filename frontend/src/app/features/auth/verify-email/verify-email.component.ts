import { Component, inject, OnInit, OnDestroy, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../../core/services/auth.service';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, InputTextModule, ButtonModule, TranslatePipe],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.scss'
})
export class VerifyEmailComponent implements OnInit, OnDestroy {
  loading       = signal(false);
  error         = signal(false);
  success       = signal(false);
  email         = signal('');
  resendSuccess = signal(false);
  resendError   = signal<string | null>(null);
  cooldown      = signal(0);

  private fb   = inject(FormBuilder);
  private auth = inject(AuthService);
  private cooldownTimer: ReturnType<typeof setInterval> | null = null;

  form = this.fb.group({
    code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
  });

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    const emailParam = this.route.snapshot.queryParamMap.get('email');
    if (emailParam) this.email.set(emailParam);
  }

  ngOnDestroy(): void {
    if (this.cooldownTimer) clearInterval(this.cooldownTimer);
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.error.set(false);
    this.auth.verifyEmail(this.email(), this.form.value.code!).subscribe({
      next: () => { this.success.set(true); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
  }

  resend(event: Event): void {
    event.preventDefault();
    if (!this.email() || this.cooldown() > 0) return;
    this.resendError.set(null);
    this.resendSuccess.set(false);

    this.auth.resendVerification(this.email()).subscribe({
      next: () => {
        this.resendSuccess.set(true);
        this.startCooldown(60);
      },
      error: (err) => {
        if (err?.status === 429) {
          this.resendError.set('AUTH.VERIFY.RESEND_LIMIT');
        } else if (err?.status === 400) {
          this.resendError.set('AUTH.VERIFY.ALREADY_VERIFIED');
        } else {
          this.resendError.set('AUTH.VERIFY.RESEND_ERROR');
        }
      }
    });
  }

  private startCooldown(seconds: number): void {
    this.cooldown.set(seconds);
    if (this.cooldownTimer) clearInterval(this.cooldownTimer);
    this.cooldownTimer = setInterval(() => {
      const next = this.cooldown() - 1;
      this.cooldown.set(next);
      if (next <= 0 && this.cooldownTimer) {
        clearInterval(this.cooldownTimer);
        this.cooldownTimer = null;
      }
    }, 1000);
  }
}
