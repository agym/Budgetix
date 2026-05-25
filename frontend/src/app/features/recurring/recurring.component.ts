import { Component, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';
import { RecurringService, RecurringTransaction } from '../../core/services/recurring.service';
import { AccountService } from '../../core/services/account.service';
import { CategoryService } from '../../core/services/category.service';
import { Account } from '../../core/models/account.model';
import { Category } from '../../core/models/category.model';

@Component({
  selector: 'app-recurring',
  standalone: true,
  imports: [
    CurrencyPipe, DatePipe, FormsModule, ReactiveFormsModule,
    ButtonModule, DialogModule, InputTextModule, DropdownModule,
    CalendarModule, TagModule, ToastModule, ConfirmDialogModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './recurring.component.html',
  styleUrl: './recurring.component.scss'
})
export class RecurringComponent implements OnInit {
  rules = signal<RecurringTransaction[]>([]);
  showForm = false;
  form!: FormGroup;

  accountOptions = signal<Account[]>([]);
  categoryOptions = signal<Category[]>([]);

  typeOptions = [
    { label: 'Expense', value: 'EXPENSE' },
    { label: 'Income',  value: 'INCOME'  }
  ];

  frequencyOptions = [
    { label: 'Daily',     value: 'DAILY'     },
    { label: 'Weekly',    value: 'WEEKLY'    },
    { label: 'Bi-weekly', value: 'BIWEEKLY'  },
    { label: 'Monthly',   value: 'MONTHLY'   },
    { label: 'Quarterly', value: 'QUARTERLY' },
    { label: 'Yearly',    value: 'YEARLY'    }
  ];

  constructor(
    private recurringService: RecurringService,
    private accountService: AccountService,
    private categoryService: CategoryService,
    private fb: FormBuilder,
    private toast: MessageService,
    private confirm: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      description: ['', Validators.required],
      amount:      [null, [Validators.required, Validators.min(0.01)]],
      type:        ['EXPENSE', Validators.required],
      accountId:   [null, Validators.required],
      categoryId:  [null],
      frequency:   ['MONTHLY', Validators.required],
      startDate:   [new Date(), Validators.required],
      endDate:     [null]
    });
    this.accountService.getAll().subscribe(a => this.accountOptions.set(a));
    this.categoryService.getAll().subscribe(cats => {
      const flat: Category[] = [];
      cats.forEach(c => { flat.push(c); flat.push(...c.children); });
      this.categoryOptions.set(flat);
    });
    this.load();
  }

  load(): void {
    this.recurringService.getAll().subscribe(r => this.rules.set(r));
  }

  openCreate(): void {
    this.form.reset({ type: 'EXPENSE', frequency: 'MONTHLY', startDate: new Date() });
    this.showForm = true;
  }

  submit(): void {
    if (this.form.invalid) return;
    const val = this.form.value;
    const req = {
      ...val,
      startDate: val.startDate instanceof Date ? val.startDate.toISOString() : val.startDate,
      endDate:   val.endDate instanceof Date   ? val.endDate.toISOString()   : val.endDate
    };
    this.recurringService.create(req).subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Rule created' });
        this.showForm = false;
        this.load();
      },
      error: (e) => this.toast.add({ severity: 'error', summary: e.error?.error || 'Error' })
    });
  }

  toggle(r: RecurringTransaction): void {
    this.recurringService.toggle(r.id).subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: r.active ? 'Paused' : 'Resumed' });
        this.load();
      },
      error: () => this.toast.add({ severity: 'error', summary: 'Failed to toggle' })
    });
  }

  deleteRule(r: RecurringTransaction): void {
    this.confirm.confirm({
      message: `Delete "${r.description}"?`,
      accept: () => this.recurringService.remove(r.id).subscribe(() => {
        this.toast.add({ severity: 'success', summary: 'Deleted' });
        this.load();
      })
    });
  }

  severityOf(type: string): 'success' | 'danger' {
    return type === 'INCOME' ? 'success' : 'danger';
  }

  frequencyLabel(f: string): string {
    return this.frequencyOptions.find(o => o.value === f)?.label ?? f;
  }
}
