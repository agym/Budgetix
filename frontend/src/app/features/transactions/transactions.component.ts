import { Component, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { SkeletonModule } from 'primeng/skeleton';
import { FileUploadModule } from 'primeng/fileupload';
import { ChipModule } from 'primeng/chip';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MessageService, ConfirmationService } from 'primeng/api';
import { TransactionService } from '../../core/services/transaction.service';
import { CategoryService } from '../../core/services/category.service';
import { AccountService } from '../../core/services/account.service';
import { DashboardService, DashboardOverview } from '../../core/services/dashboard.service';
import { Transaction } from '../../core/models/transaction.model';
import { Category } from '../../core/models/category.model';
import { Account } from '../../core/models/account.model';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [
    CurrencyPipe, DatePipe, DecimalPipe, FormsModule, ReactiveFormsModule,
    TableModule, ButtonModule, DialogModule, InputTextModule,
    DropdownModule, CalendarModule, TagModule, ToastModule,
    ConfirmDialogModule, SkeletonModule, FileUploadModule, ChipModule, InputTextareaModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.scss'
})
export class TransactionsComponent implements OnInit {
  transactions  = signal<Transaction[]>([]);
  totalRecords  = signal(0);
  loading       = signal(false);
  submitting    = signal(false);
  statsLoading  = signal(true);
  overview      = signal<DashboardOverview | null>(null);
  accountOptions  = signal<Account[]>([]);
  flatCategories  = signal<Category[]>([]);
  selectedTxs: Transaction[] = [];

  showForm   = false;
  showImport = false;
  editId: string | null = null;
  search      = '';
  filterType  = '';
  filterAccount = '';
  importAccountId = '';
  csvFile: File | null = null;
  tagInput = '';
  formTags: string[] = [];

  typeOptions = [
    { label: 'Income',   value: 'INCOME' },
    { label: 'Expense',  value: 'EXPENSE' },
    { label: 'Transfer', value: 'TRANSFER' }
  ];

  form!: FormGroup;
  private currentPage = 0;

  constructor(
    private txService: TransactionService,
    private categoryService: CategoryService,
    private accountService: AccountService,
    private dashboardService: DashboardService,
    private fb: FormBuilder,
    private toast: MessageService,
    private confirm: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      type:        ['EXPENSE', Validators.required],
      amount:      [null, [Validators.required, Validators.min(0.01)]],
      accountId:   [null, Validators.required],
      categoryId:  [null],
      description: [''],
      notes:       [''],
      date:        [new Date(), Validators.required]
    });

    this.accountService.getAll().subscribe(a => this.accountOptions.set(a));
    this.categoryService.getAll().subscribe(cats => {
      const flat: Category[] = [];
      cats.forEach(c => { flat.push(c); flat.push(...(c.children ?? [])); });
      this.flatCategories.set(flat);
    });
    this.dashboardService.getOverview().subscribe({
      next: o  => { this.overview.set(o); this.statsLoading.set(false); },
      error: () => this.statsLoading.set(false)
    });
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.loading.set(true);
    this.txService.getAll({
      page: this.currentPage, size: 20,
      search: this.search || undefined,
      type: this.filterType || undefined,
      accountId: this.filterAccount || undefined
    }).subscribe({
      next: page => { this.transactions.set(page.content); this.totalRecords.set(page.totalElements); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  onLazyLoad(event: any): void {
    this.currentPage = event.first / event.rows;
    this.loadTransactions();
  }

  onSearch(): void { this.currentPage = 0; this.loadTransactions(); }

  openCreate(): void {
    this.editId = null;
    this.formTags = [];
    this.tagInput = '';
    this.form.reset({ type: 'EXPENSE', date: new Date() });
    this.showForm = true;
  }

  openEdit(tx: Transaction): void {
    this.editId = tx.id;
    this.formTags = [...(tx.tags ?? [])];
    this.tagInput = '';
    this.form.patchValue({
      type: tx.type, amount: tx.amount,
      accountId: tx.account.id, categoryId: tx.category?.id,
      description: tx.description, notes: tx.notes ?? '',
      date: new Date(tx.date)
    });
    this.showForm = true;
  }

  addTag(): void {
    const t = this.tagInput.trim().toLowerCase();
    if (t && !this.formTags.includes(t)) this.formTags.push(t);
    this.tagInput = '';
  }

  removeTag(tag: string): void {
    this.formTags = this.formTags.filter(t => t !== tag);
  }

  submit(): void {
    if (this.form.invalid) return;
    this.submitting.set(true);
    const val = this.form.value;
    const req = { ...val, date: val.date.toISOString(), tags: this.formTags };
    const obs = this.editId ? this.txService.update(this.editId, req) : this.txService.create(req);
    obs.subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Success', detail: this.editId ? 'Transaction updated' : 'Transaction created' });
        this.showForm = false; this.submitting.set(false);
        this.loadTransactions();
        this.refreshStats();
      },
      error: () => { this.toast.add({ severity: 'error', summary: 'Error', detail: 'Something went wrong' }); this.submitting.set(false); }
    });
  }

  confirmDelete(tx: Transaction): void {
    this.confirm.confirm({
      message: 'Delete this transaction?',
      header: 'Confirm Delete',
      icon: 'pi pi-trash',
      accept: () => {
        this.txService.remove(tx.id).subscribe(() => {
          this.toast.add({ severity: 'success', summary: 'Deleted' });
          this.loadTransactions(); this.refreshStats();
        });
      }
    });
  }

  bulkDelete(): void {
    this.confirm.confirm({
      message: `Delete ${this.selectedTxs.length} selected transactions?`,
      header: 'Bulk Delete',
      icon: 'pi pi-trash',
      accept: () => {
        const ids = this.selectedTxs.map(t => t.id);
        this.txService.bulkDelete(ids).subscribe(() => {
          this.selectedTxs = [];
          this.toast.add({ severity: 'success', summary: 'Deleted' });
          this.loadTransactions(); this.refreshStats();
        });
      }
    });
  }

  onCsvSelected(event: any): void { this.csvFile = event.files[0]; }

  importCsv(): void {
    if (!this.csvFile || !this.importAccountId) return;
    this.txService.importCsv(this.importAccountId, this.csvFile).subscribe({
      next: txs => {
        this.toast.add({ severity: 'success', summary: `Imported ${txs.length} transactions` });
        this.showImport = false; this.loadTransactions(); this.refreshStats();
      },
      error: () => this.toast.add({ severity: 'error', summary: 'Import failed' })
    });
  }

  clearFilters(): void {
    this.search = ''; this.filterType = ''; this.filterAccount = '';
    this.currentPage = 0; this.loadTransactions();
  }

  get hasFilters(): boolean {
    return !!(this.search || this.filterType || this.filterAccount);
  }

  private refreshStats(): void {
    this.dashboardService.getOverview().subscribe(o => this.overview.set(o));
  }
}
