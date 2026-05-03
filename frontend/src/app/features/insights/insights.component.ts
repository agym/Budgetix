import { Component, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { HttpClient } from '@angular/common/http';

interface Insight {
  id: string;
  type: string;
  title: string;
  message: string;
  period?: string;
  createdAt: string;
}

@Component({
  selector: 'app-insights',
  standalone: true,
  imports: [DatePipe, ButtonModule, ToastModule],
  providers: [MessageService],
  templateUrl: './insights.component.html',
  styleUrl: './insights.component.scss'
})
export class InsightsComponent extends ApiService implements OnInit {
  insights = signal<Insight[]>([]);
  generating = signal(false);

  constructor(http: HttpClient, private toast: MessageService) {
    super(http);
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.get<Insight[]>('/insights').subscribe(data => this.insights.set(data));
  }

  generate(): void {
    this.generating.set(true);
    this.post<Insight[]>('/insights/generate', {}).subscribe({
      next: (data) => {
        this.insights.set([...data, ...this.insights()]);
        this.toast.add({ severity: 'success', summary: `Generated ${data.length} new insights` });
        this.generating.set(false);
      },
      error: () => {
        this.toast.add({ severity: 'error', summary: 'Error generating insights' });
        this.generating.set(false);
      }
    });
  }

  dismiss(insight: Insight): void {
    this.delete<void>(`/insights/${insight.id}/dismiss`).subscribe(() => {
      this.insights.set(this.insights().filter(i => i.id !== insight.id));
    });
  }

  getIcon(type: string): string {
    const icons: Record<string, string> = {
      LOW_SAVINGS_RATE: 'pi-exclamation-triangle',
      HIGH_SAVINGS_RATE: 'pi-star',
      SPENDING_INCREASE: 'pi-trending-up',
      SPENDING_DECREASE: 'pi-trending-down',
      SUBSCRIPTION_SUMMARY: 'pi-sync',
      MONTHLY_SUMMARY: 'pi-chart-bar',
      GOAL_MILESTONE: 'pi-flag',
      UNUSUAL_SPENDING: 'pi-exclamation-circle',
    };
    return icons[type] ?? 'pi-lightbulb';
  }

  getCategory(type: string): string {
    if (['HIGH_SAVINGS_RATE', 'SPENDING_DECREASE', 'GOAL_MILESTONE', 'BUDGET_ON_TRACK'].includes(type)) return 'positive';
    if (['LOW_SAVINGS_RATE', 'SPENDING_INCREASE', 'UNUSUAL_SPENDING', 'BUDGET_EXCEEDED'].includes(type)) return 'negative';
    if (['MONTHLY_SUMMARY', 'SUBSCRIPTION_SUMMARY'].includes(type)) return 'info';
    return 'warning';
  }
}
