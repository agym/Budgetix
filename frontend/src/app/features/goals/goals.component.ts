import { Component, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { CalendarModule } from 'primeng/calendar';
import { ProgressBarModule } from 'primeng/progressbar';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';
import { GoalService } from '../../core/services/goal.service';
import { Goal } from '../../core/models/goal.model';

@Component({
  selector: 'app-goals',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, DecimalPipe, FormsModule, ReactiveFormsModule, ButtonModule, DialogModule,
    InputTextModule, CalendarModule, ProgressBarModule, ToastModule, ConfirmDialogModule],
  providers: [MessageService, ConfirmationService],
  templateUrl: './goals.component.html',
  styleUrl: './goals.component.scss'
})
export class GoalsComponent implements OnInit {
  goals = signal<Goal[]>([]);
  showForm = false;
  showContribute = false;
  selectedGoal: Goal | null = null;
  contributeAmount = 0;
  contributeNote = '';
  form!: FormGroup;

  constructor(
    private goalService: GoalService,
    private fb: FormBuilder,
    private toast: MessageService,
    private confirm: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', Validators.required],
      targetAmount: [null, [Validators.required, Validators.min(0.01)]],
      deadline: [null],
      color: ['#6366f1'],
      icon: ['pi-flag']
    });
    this.load();
  }

  load(): void {
    this.goalService.getAll().subscribe(g => this.goals.set(g));
  }

  openCreate(): void {
    this.form.reset({ color: '#6366f1', icon: 'pi-flag' });
    this.showForm = true;
  }

  submit(): void {
    if (this.form.invalid) return;
    const val = this.form.value;
    const req = { ...val, deadline: val.deadline?.toISOString() };
    this.goalService.create(req).subscribe({
      next: () => { this.toast.add({ severity: 'success', summary: 'Goal created' }); this.showForm = false; this.load(); },
      error: (e) => this.toast.add({ severity: 'error', summary: e.error?.error || 'Error' })
    });
  }

  openContribute(g: Goal): void {
    this.selectedGoal = g;
    this.contributeAmount = 0;
    this.contributeNote = '';
    this.showContribute = true;
  }

  contribute(): void {
    if (!this.selectedGoal || !this.contributeAmount) return;
    this.goalService.contribute(this.selectedGoal.id, this.contributeAmount, this.contributeNote).subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Contribution added' });
        this.showContribute = false;
        this.load();
      },
      error: () => this.toast.add({ severity: 'error', summary: 'Error' })
    });
  }

  deleteGoal(g: Goal): void {
    this.confirm.confirm({
      message: `Delete goal "${g.name}"?`,
      accept: () => this.goalService.remove(g.id).subscribe(() => {
        this.toast.add({ severity: 'success', summary: 'Deleted' }); this.load();
      })
    });
  }
}
