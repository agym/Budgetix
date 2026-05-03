import { Component, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { NgxEchartsDirective } from 'ngx-echarts';
import { EChartsOption } from 'echarts';
import { SkeletonModule } from 'primeng/skeleton';
import { forkJoin } from 'rxjs';
import { DashboardService, DashboardOverview } from '../../core/services/dashboard.service';
import { CategoryService } from '../../core/services/category.service';
import { Category } from '../../core/models/category.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CurrencyPipe, DecimalPipe, NgxEchartsDirective, SkeletonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  loading = signal(true);
  overview = signal<DashboardOverview | null>(null);
  barChartOptions = signal<EChartsOption>({});
  pieChartOptions = signal<EChartsOption>({});
  lineChartOptions = signal<EChartsOption>({});

  constructor(
    private dashboardService: DashboardService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    const now = new Date();
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString();
    const lastDay  = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString();

    forkJoin({
      overview:      this.dashboardService.getOverview(),
      incomeVsExp:   this.dashboardService.getIncomeVsExpenses(6),
      spendingByCat: this.dashboardService.getSpendingByCategory(firstDay, lastDay),
      dailyTrend:    this.dashboardService.getDailyTrend(now.getMonth() + 1, now.getFullYear()),
      categories:    this.categoryService.getAll()
    }).subscribe({
      next: ({ overview, incomeVsExp, spendingByCat, dailyTrend, categories }) => {
        this.overview.set(overview);
        this.buildBarChart(incomeVsExp);
        this.buildPieChart(spendingByCat, categories);
        this.buildLineChart(dailyTrend);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  private buildBarChart(data: any[]): void {
    this.barChartOptions.set({
      tooltip: { trigger: 'axis' },
      legend: { data: ['Income', 'Expenses'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: data.map(d => d.month) },
      yAxis: { type: 'value' },
      series: [
        { name: 'Income',   type: 'bar', data: data.map(d => d.income),   color: '#22c55e', barMaxWidth: 32, itemStyle: { borderRadius: [4,4,0,0] } },
        { name: 'Expenses', type: 'bar', data: data.map(d => d.expenses), color: '#ef4444', barMaxWidth: 32, itemStyle: { borderRadius: [4,4,0,0] } }
      ]
    });
  }

  private buildPieChart(data: any[], categories: Category[]): void {
    const catMap = new Map(categories.flatMap(c => [c, ...(c.children ?? [])]).map(c => [c.id, c]));
    this.pieChartOptions.set({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { orient: 'vertical', right: '5%', top: 'center' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        data: data.map(d => ({
          value: d.amount,
          name: catMap.get(d.categoryId)?.name ?? 'Other',
          itemStyle: { color: catMap.get(d.categoryId)?.color }
        }))
      }]
    });
  }

  private buildLineChart(data: any[]): void {
    this.lineChartOptions.set({
      tooltip: { trigger: 'axis' },
      legend: { data: ['Income', 'Expenses', 'Running Net'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: data.map(d => d.date?.toString().substring(5)) },
      yAxis: { type: 'value' },
      series: [
        { name: 'Income',      type: 'bar',  data: data.map(d => d.income),     color: '#22c55e', barMaxWidth: 20, itemStyle: { borderRadius: [3,3,0,0] } },
        { name: 'Expenses',    type: 'bar',  data: data.map(d => d.expenses),   color: '#ef4444', barMaxWidth: 20, itemStyle: { borderRadius: [3,3,0,0] } },
        { name: 'Running Net', type: 'line', data: data.map(d => d.runningNet), color: '#6366f1', smooth: true,
          lineStyle: { width: 2 }, areaStyle: { opacity: 0.08 },
          markLine: { data: [{ type: 'average', name: 'Avg' }], silent: true } }
      ]
    });
  }
}
