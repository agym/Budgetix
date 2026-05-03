import { Component, OnInit, signal } from '@angular/core';
import { CurrencyPipe, TitleCasePipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';
import { AccountService } from '../../core/services/account.service';
import { Account, AccountType } from '../../core/models/account.model';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CurrencyPipe, TitleCasePipe, FormsModule, ReactiveFormsModule, ButtonModule, DialogModule,
    InputTextModule, DropdownModule, ToastModule, ConfirmDialogModule],
  providers: [MessageService, ConfirmationService],
  templateUrl: './accounts.component.html',
  styleUrl: './accounts.component.scss'
})
export class AccountsComponent implements OnInit {
  accounts = signal<Account[]>([]);
  showForm = false;
  editId: string | null = null;
  form!: FormGroup;

  typeOptions = [
    { label: 'Cash', value: 'CASH' },
    { label: 'Bank Account', value: 'BANK' },
    { label: 'Credit Card', value: 'CREDIT_CARD' },
    { label: 'Savings', value: 'SAVINGS' },
    { label: 'Investment', value: 'INVESTMENT' }
  ];

  constructor(
    private accountService: AccountService,
    private fb: FormBuilder,
    private toast: MessageService,
    private confirm: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', Validators.required],
      type: ['BANK', Validators.required],
      initialBalance: [0],
      currency: ['USD'],
      color: ['#6366f1']
    });
    this.load();
  }

  load(): void {
    this.accountService.getAll().subscribe(a => this.accounts.set(a));
  }

  openCreate(): void {
    this.editId = null;
    this.form.reset({ type: 'BANK', currency: 'USD', color: '#6366f1', initialBalance: 0 });
    this.showForm = true;
  }

  openEdit(a: Account): void {
    this.editId = a.id;
    this.form.patchValue({ name: a.name, type: a.type, currency: a.currency, color: a.color });
    this.showForm = true;
  }

  submit(): void {
    if (this.form.invalid) return;
    const req = this.form.value;
    const obs = this.editId
      ? this.accountService.update(this.editId, req)
      : this.accountService.create(req);
    obs.subscribe({
      next: () => { this.toast.add({ severity: 'success', summary: 'Saved' }); this.showForm = false; this.load(); },
      error: (e) => this.toast.add({ severity: 'error', summary: e.error?.error || 'Error' })
    });
  }

  confirmDelete(a: Account): void {
    this.confirm.confirm({
      message: `Delete "${a.name}"? This cannot be undone.`,
      accept: () => this.accountService.remove(a.id).subscribe({
        next: () => { this.toast.add({ severity: 'success', summary: 'Deleted' }); this.load(); },
        error: (e) => this.toast.add({ severity: 'error', summary: e.error?.error || 'Error' })
      })
    });
  }
}
