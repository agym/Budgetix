import { Component, OnInit, signal } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ChipModule } from 'primeng/chip';
import { MessageService, ConfirmationService } from 'primeng/api';
import { CategoryService } from '../../core/services/category.service';
import { Category } from '../../core/models/category.model';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [
    FormsModule, ReactiveFormsModule,
    ButtonModule, DialogModule, InputTextModule, DropdownModule,
    TagModule, ToastModule, ConfirmDialogModule, ChipModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './categories.component.html',
  styleUrl: './categories.component.scss'
})
export class CategoriesComponent implements OnInit {
  categories = signal<Category[]>([]);
  showForm = false;
  showRuleDialog = false;
  editTarget: Category | null = null;
  ruleTarget: Category | null = null;
  newKeyword = '';
  form!: FormGroup;

  typeOptions = [
    { label: 'Expense',  value: 'EXPENSE'  },
    { label: 'Income',   value: 'INCOME'   },
    { label: 'Transfer', value: 'TRANSFER' }
  ];

  iconOptions = [
    'pi-shopping-cart', 'pi-car', 'pi-home', 'pi-briefcase', 'pi-heart',
    'pi-star', 'pi-bolt', 'pi-wifi', 'pi-phone', 'pi-graduation-cap',
    'pi-gift', 'pi-ticket', 'pi-file', 'pi-dollar', 'pi-credit-card',
    'pi-wallet', 'pi-flag', 'pi-map-marker', 'pi-tag', 'pi-sync'
  ];

  parentOptions = signal<Category[]>([]);

  constructor(
    private categoryService: CategoryService,
    private fb: FormBuilder,
    private toast: MessageService,
    private confirm: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name:     ['', Validators.required],
      type:     ['EXPENSE', Validators.required],
      icon:     ['pi-tag'],
      color:    ['#6366f1'],
      parentId: [null]
    });
    this.load();
  }

  load(): void {
    this.categoryService.getAll().subscribe(cats => {
      this.categories.set(cats);
      this.parentOptions.set(cats.filter(c => !c.parentId));
    });
  }

  openCreate(): void {
    this.editTarget = null;
    this.form.reset({ type: 'EXPENSE', icon: 'pi-tag', color: '#6366f1' });
    this.showForm = true;
  }

  openEdit(c: Category): void {
    this.editTarget = c;
    this.form.patchValue({ name: c.name, type: c.type, icon: c.icon, color: c.color, parentId: c.parentId });
    this.showForm = true;
  }

  submit(): void {
    if (this.form.invalid) return;
    const req = this.form.value;
    const obs = this.editTarget
      ? this.categoryService.update(this.editTarget.id, req)
      : this.categoryService.create(req);
    obs.subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: this.editTarget ? 'Category updated' : 'Category created' });
        this.showForm = false;
        this.load();
      },
      error: (e) => this.toast.add({ severity: 'error', summary: e.error?.error || 'Error' })
    });
  }

  deleteCategory(c: Category): void {
    this.confirm.confirm({
      message: `Delete "${c.name}"? Any transactions using it will be uncategorized.`,
      accept: () => this.categoryService.remove(c.id).subscribe({
        next: () => { this.toast.add({ severity: 'success', summary: 'Deleted' }); this.load(); },
        error: (e) => this.toast.add({ severity: 'error', summary: e.error?.error || 'Cannot delete' })
      })
    });
  }

  openRules(c: Category): void {
    this.ruleTarget = c;
    this.newKeyword = '';
    this.showRuleDialog = true;
  }

  addRule(): void {
    if (!this.ruleTarget || !this.newKeyword.trim()) return;
    this.categoryService.addRule(this.ruleTarget.id, this.newKeyword.trim()).subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Rule added' });
        this.newKeyword = '';
      },
      error: () => this.toast.add({ severity: 'error', summary: 'Failed to add rule' })
    });
  }

  typeSeverity(type: string): 'success' | 'danger' | 'info' {
    return type === 'INCOME' ? 'success' : type === 'EXPENSE' ? 'danger' : 'info';
  }

  userCategories(): Category[] {
    return this.categories().filter(c => !c.system);
  }

  systemCategories(): Category[] {
    return this.categories().filter(c => c.system);
  }
}
