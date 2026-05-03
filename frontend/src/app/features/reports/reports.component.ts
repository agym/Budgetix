import { Component, OnInit, signal, computed } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgxEchartsDirective } from 'ngx-echarts';
import { EChartsOption } from 'echarts';
import { DropdownModule } from 'primeng/dropdown';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { HttpClient } from '@angular/common/http';

interface CategoryBreakdown {
  categoryId: string;
  categoryName: string;
  amount: number;
  percentage: number;
  transactionCount: number;
}

interface MonthlyReport {
  month: string;
  year: number;
  totalIncome: number;
  totalExpenses: number;
  netSavings: number;
  savingsRate: number;
  categoryBreakdown: CategoryBreakdown[];
  topExpenseCategory: string;
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CurrencyPipe, DecimalPipe, FormsModule, NgxEchartsDirective, DropdownModule, ButtonModule, TableModule, TagModule, ToastModule],
  providers: [MessageService],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent extends ApiService implements OnInit {
  report = signal<MonthlyReport | null>(null);
  loading = signal(false);
  exporting = signal(false);
  exportingPdf = signal(false);
  pieOptions = signal<EChartsOption>({});
  gaugeOptions = signal<EChartsOption>({});

  selectedMonth: number;
  selectedYear: number;

  monthOptions = [
    { label: 'January', value: 1 }, { label: 'February', value: 2 }, { label: 'March', value: 3 },
    { label: 'April', value: 4 }, { label: 'May', value: 5 }, { label: 'June', value: 6 },
    { label: 'July', value: 7 }, { label: 'August', value: 8 }, { label: 'September', value: 9 },
    { label: 'October', value: 10 }, { label: 'November', value: 11 }, { label: 'December', value: 12 }
  ];

  yearOptions = [2023, 2024, 2025, 2026].map(y => ({ label: y.toString(), value: y }));

  constructor(http: HttpClient, private toast: MessageService) {
    super(http);
    const now = new Date();
    this.selectedMonth = now.getMonth() + 1;
    this.selectedYear = now.getFullYear();
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.get<MonthlyReport>('/reports/monthly', { month: this.selectedMonth, year: this.selectedYear })
      .subscribe({
        next: data => {
          this.report.set(data);
          this.buildCharts(data);
          this.loading.set(false);
        },
        error: () => {
          this.report.set(null);
          this.loading.set(false);
        }
      });
  }

  private buildCharts(data: MonthlyReport): void {
    const breakdown = data.categoryBreakdown ?? [];
    this.pieOptions.set({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { fontSize: 11 } },
      series: [{
        type: 'pie',
        radius: ['40%', '68%'],
        center: ['38%', '50%'],
        data: breakdown.map(d => ({ value: d.amount, name: d.categoryName }))
      }]
    });

    const savingsPct = Math.max(0, Math.min(100, data.savingsRate));
    this.gaugeOptions.set({
      series: [{
        type: 'gauge',
        startAngle: 200, endAngle: -20,
        min: 0, max: 100,
        pointer: { show: true, length: '60%' },
        progress: { show: true, width: 18 },
        axisLine: { lineStyle: { width: 18 } },
        axisTick: { show: false },
        splitLine: { length: 12, lineStyle: { width: 2 } },
        axisLabel: { distance: 25, fontSize: 10 },
        anchor: { show: false },
        title: { show: true, offsetCenter: [0, '70%'], fontSize: 12 },
        detail: { valueAnimation: true, fontSize: 28, offsetCenter: [0, '40%'], formatter: '{value}%' },
        data: [{ value: parseFloat(savingsPct.toFixed(1)), name: 'Savings Rate' }]
      }]
    });
  }

  exportCsv(): void {
    this.exporting.set(true);
    this.getBlob('/reports/export/csv', { month: this.selectedMonth, year: this.selectedYear })
      .subscribe({
        next: blob => {
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `report-${this.selectedYear}-${String(this.selectedMonth).padStart(2, '0')}.csv`;
          a.click();
          URL.revokeObjectURL(url);
          this.exporting.set(false);
        },
        error: () => {
          this.toast.add({ severity: 'error', summary: 'Export failed' });
          this.exporting.set(false);
        }
      });
  }

  exportPdf(): void {
    this.exportingPdf.set(true);
    this.getBlob('/reports/export/pdf', { month: this.selectedMonth, year: this.selectedYear })
      .subscribe({
        next: blob => {
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `report-${this.selectedYear}-${String(this.selectedMonth).padStart(2, '0')}.pdf`;
          a.click();
          URL.revokeObjectURL(url);
          this.exportingPdf.set(false);
        },
        error: () => {
          this.toast.add({ severity: 'error', summary: 'PDF export failed' });
          this.exportingPdf.set(false);
        }
      });
  }
}
