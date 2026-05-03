import { Component, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { ProgressBarModule } from 'primeng/progressbar';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';
import { BudgetService } from '../../core/services/budget.service';
import { CategoryService } from '../../core/services/category.service';
import { Budget } from '../../core/models/budget.model';
import { Category } from '../../core/models/category.model';

@Component({
  selector: 'app-budgets',
  standalone: true,
  imports: [CurrencyPipe, DecimalPipe, FormsModule, ReactiveFormsModule, ButtonModule, DialogModule,
    InputTextModule, DropdownModule, ProgressBarModule, ToastModule, ConfirmDialogModule],
  providers: [MessageService, ConfirmationService],
  templateUrl: './budgets.component.html',
  styleUrl: './budgets.component.scss'
})
export class BudgetsComponent implements OnInit {
  budgets = signal<Budget[]>([]);
  categoryOptions = signal<Category[]>([]);
  showForm = false;
  form!: FormGroup;
  selectedMonth = new Date().getMonth() + 1;
  selectedYear = new Date().getFullYear();
  monthOptions = Array.from({ length: 12 }, (_, i) => ({
    label: new Date(0, i).toLocaleString('default', { month: 'long' }), value: i + 1
  }));
  yearOptions = [2023, 2024, 2025, 2026].map(y => ({ label: y.toString(), value: y }));

  constructor(
    private budgetService: BudgetService,
    private categoryService: CategoryService,
    private fb: FormBuilder,
    private toast: MessageService,
    private confirm: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      categoryId: [null],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      rollover: [false]
    });
    this.categoryService.getAll().subscribe(cats => {
      const flat: Category[] = [];
      cats.filter(c => c.type === 'EXPENSE').forEach(c => {
        flat.push(c); flat.push(...c.children.filter(ch => ch.type === 'EXPENSE'));
      });
      this.categoryOptions.set(flat);
    });
    this.load();
  }

  load(): void {
    this.budgetService.getByPeriod(this.selectedMonth, this.selectedYear)
      .subscribe(b => this.budgets.set(b));
  }

  openCreate(): void {
    this.form.reset({ rollover: false });
    this.showForm = true;
  }

  submit(): void {
    if (this.form.invalid) return;
    const req = { ...this.form.value, month: this.selectedMonth, year: this.selectedYear };
    this.budgetService.create(req).subscribe({
      next: () => { this.toast.add({ severity: 'success', summary: 'Budget created' }); this.showForm = false; this.load(); },
      error: (e) => this.toast.add({ severity: 'error', summary: e.error?.error || 'Error' })
    });
  }

  deleteBudget(b: Budget): void {
    this.confirm.confirm({
      message: 'Delete this budget?',
      accept: () => this.budgetService.remove(b.id).subscribe(() => {
        this.toast.add({ severity: 'success', summary: 'Deleted' }); this.load();
      })
    });
  }
}
