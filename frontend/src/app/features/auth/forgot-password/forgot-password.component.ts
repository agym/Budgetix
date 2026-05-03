import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../../core/services/auth.service';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, InputTextModule, ButtonModule, TranslatePipe],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss'
})
export class ForgotPasswordComponent {
  loading = signal(false);
  sent    = signal(false);
  error   = signal(false);

  private fb   = inject(FormBuilder);
  private auth = inject(AuthService);

  form = this.fb.group({ email: ['', [Validators.required, Validators.email]] });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.error.set(false);
    this.auth.forgotPassword(this.form.value.email!).subscribe({
      next: () => { this.sent.set(true); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
  }
}
