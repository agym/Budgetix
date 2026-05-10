import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../../core/services/auth.service';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, InputTextModule, ButtonModule, TranslatePipe],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  loading     = signal(false);
  error       = signal<string | null>(null);
  success     = signal(false);
  showPw      = signal(false);
  showConfirm = signal(false);

  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    name:            ['', Validators.required],
    email:           ['', [Validators.required, Validators.email]],
    password:        ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required]
  }, { validators: this.passwordMatchValidator });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.error.set(null);
    const { name, email, password } = this.form.value;
    this.auth.register({ name: name!, email: email!, password: password! }).subscribe({
      next: () => {
          this.loading.set(false);
          this.router.navigate(['/auth/verify-email'], { queryParams: { email: email! } });
        },
      error: (err) => {
        const msg: string = err?.error?.message ?? '';
        this.error.set(msg.includes('already') ? 'AUTH.REGISTER.EMAIL_EXISTS' : 'AUTH.REGISTER.FAILED');
        this.loading.set(false);
      }
    });
  }

  private passwordMatchValidator(form: AbstractControl) {
    const pw      = form.get('password')?.value;
    const confirm = form.get('confirmPassword')?.value;
    return pw && confirm && pw !== confirm ? { mismatch: true } : null;
  }
}
