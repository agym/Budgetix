import { Component, inject, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../../core/services/auth.service';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, InputTextModule, ButtonModule, TranslatePipe],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit {
  loading     = signal(false);
  error       = signal(false);
  success     = signal(false);
  showPw      = signal(false);
  showConfirm = signal(false);
  email       = signal('');

  private fb   = inject(FormBuilder);
  private auth = inject(AuthService);

  form = this.fb.group({
    code:            ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
    newPassword:     ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required]
  }, { validators: this.passwordMatchValidator });

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    const emailParam = this.route.snapshot.queryParamMap.get('email');
    if (emailParam) this.email.set(emailParam);
  }

  submit(): void {
    if (this.form.invalid || this.form.errors?.['mismatch']) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.error.set(false);
    const { code, newPassword } = this.form.value;
    this.auth.resetPassword(this.email(), code!, newPassword!).subscribe({
      next: () => { this.success.set(true); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
  }

  private passwordMatchValidator(form: AbstractControl) {
    const pw = form.get('newPassword')?.value;
    const confirm = form.get('confirmPassword')?.value;
    return pw && confirm && pw !== confirm ? { mismatch: true } : null;
  }
}
