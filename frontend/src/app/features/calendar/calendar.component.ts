import { Component, OnInit, signal, computed } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { SkeletonModule } from 'primeng/skeleton';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { TransactionService } from '../../core/services/transaction.service';
import { Transaction } from '../../core/models/transaction.model';

interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  transactions: Transaction[];
  totalIncome: number;
  totalExpense: number;
}

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CurrencyPipe, SkeletonModule, ToastModule],
  providers: [MessageService],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.scss'
})
export class CalendarComponent implements OnInit {
  loading = signal(true);
  viewDate = signal(new Date());
  transactions = signal<Transaction[]>([]);

  readonly weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  monthLabel = computed(() => {
    const d = this.viewDate();
    return d.toLocaleString('default', { month: 'long', year: 'numeric' });
  });

  days = computed<CalendarDay[]>(() => {
    const vd = this.viewDate();
    const txs = this.transactions();
    const year = vd.getFullYear();
    const month = vd.getMonth();
    const today = new Date();

    const firstDay = new Date(year, month, 1);
    const lastDay  = new Date(year, month + 1, 0);

    // Pad to start on Sunday
    const startPad = firstDay.getDay();
    const endPad   = 6 - lastDay.getDay();

    const days: CalendarDay[] = [];

    // Previous month padding
    for (let i = startPad - 1; i >= 0; i--) {
      const date = new Date(year, month, -i);
      days.push(this.makeDay(date, false, today, txs));
    }
    // Current month
    for (let d = 1; d <= lastDay.getDate(); d++) {
      const date = new Date(year, month, d);
      days.push(this.makeDay(date, true, today, txs));
    }
    // Next month padding
    for (let i = 1; i <= endPad; i++) {
      const date = new Date(year, month + 1, i);
      days.push(this.makeDay(date, false, today, txs));
    }

    return days;
  });

  monthIncome = computed(() =>
    this.days().filter(d => d.isCurrentMonth).reduce((s, d) => s + d.totalIncome, 0));

  monthExpense = computed(() =>
    this.days().filter(d => d.isCurrentMonth).reduce((s, d) => s + d.totalExpense, 0));

  constructor(private txService: TransactionService) {}

  ngOnInit(): void {
    this.load();
  }

  prev(): void {
    const d = this.viewDate();
    this.viewDate.set(new Date(d.getFullYear(), d.getMonth() - 1, 1));
    this.load();
  }

  next(): void {
    const d = this.viewDate();
    this.viewDate.set(new Date(d.getFullYear(), d.getMonth() + 1, 1));
    this.load();
  }

  today(): void {
    this.viewDate.set(new Date());
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    const vd = this.viewDate();
    const year = vd.getFullYear();
    const month = vd.getMonth();
    const startDate = new Date(year, month, 1).toISOString();
    const endDate   = new Date(year, month + 1, 0, 23, 59, 59).toISOString();

    this.txService.getAll({ startDate, endDate, size: 500 }).subscribe({
      next: page => {
        this.transactions.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  private makeDay(date: Date, isCurrentMonth: boolean, today: Date, txs: Transaction[]): CalendarDay {
    const dayTxs = txs.filter(t => {
      const td = new Date(t.date);
      return td.getFullYear() === date.getFullYear() &&
             td.getMonth()    === date.getMonth()    &&
             td.getDate()     === date.getDate();
    });
    const totalIncome  = dayTxs.filter(t => t.type === 'INCOME').reduce((s, t) => s + t.amount, 0);
    const totalExpense = dayTxs.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + t.amount, 0);
    const isToday = date.toDateString() === today.toDateString();
    return { date, isCurrentMonth, isToday, transactions: dayTxs, totalIncome, totalExpense };
  }
}
