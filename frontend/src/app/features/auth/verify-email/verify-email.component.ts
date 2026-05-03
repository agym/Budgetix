import { Component, inject, OnInit, signal } from '@angular/core';
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
export class VerifyEmailComponent implements OnInit {
  loading = signal(false);
  error   = signal(false);
  success = signal(false);
  email   = signal('');

  private fb   = inject(FormBuilder);
  private auth = inject(AuthService);

  form = this.fb.group({
    code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
  });

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    const emailParam = this.route.snapshot.queryParamMap.get('email');
    if (emailParam) this.email.set(emailParam);
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
    if (!this.email()) return;
    this.auth.forgotPassword(this.email()).subscribe({ error: () => {} });
  }
}
