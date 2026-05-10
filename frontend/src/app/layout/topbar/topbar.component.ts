import { Component, Output, EventEmitter, OnInit, OnDestroy, signal, inject, ViewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { OverlayPanel } from 'primeng/overlaypanel';
import { TranslatePipe } from '@ngx-translate/core';
import { Subscription, interval } from 'rxjs';
import { startWith } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService, Notification } from '../../core/services/notification.service';
import { ThemeService } from '../../core/services/theme.service';
import { LanguageSwitcherComponent } from '../../shared/components/language-switcher/language-switcher.component';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [RouterLink, DatePipe, ButtonModule, OverlayPanelModule, TranslatePipe, LanguageSwitcherComponent],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss'
})
export class TopbarComponent implements OnInit, OnDestroy {
  @Output() menuToggle = new EventEmitter<void>();
  @ViewChild('notifPanel') notifPanel!: OverlayPanel;

  private auth         = inject(AuthService);
  private notifService = inject(NotificationService);
  readonly themeService = inject(ThemeService);

  user         = this.auth.currentUser;
  unreadCount  = signal(0);
  notifications = signal<Notification[]>([]);
  loadingNotifs = signal(false);

  initials = () => {
    const name = this.user()?.name ?? 'U';
    return name.split(' ').map((n: string) => n[0]).join('').substring(0, 2).toUpperCase();
  };

  private pollSub?: Subscription;

  ngOnInit(): void {
    this.pollSub = interval(30000).pipe(startWith(0)).subscribe(() => {
      this.notifService.getUnreadCount().subscribe(r => this.unreadCount.set(r.count));
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  openNotifications(event: Event): void {
    this.notifPanel.toggle(event);
    this.loadNotifications();
  }

  private loadNotifications(): void {
    this.loadingNotifs.set(true);
    this.notifService.getAll(false, 0, 10).subscribe({
      next: page => { this.notifications.set(page.content); this.loadingNotifs.set(false); },
      error: ()   => { this.loadingNotifs.set(false); }
    });
  }

  markRead(n: Notification, event: Event): void {
    event.stopPropagation();
    if (n.read) return;
    this.notifService.markRead(n.id).subscribe(() => {
      this.notifications.update(list => list.map(x => x.id === n.id ? { ...x, read: true } : x));
      this.unreadCount.update(c => Math.max(0, c - 1));
    });
  }

  markAllRead(): void {
    this.notifService.markAllRead().subscribe(() => {
      this.notifications.update(list => list.map(x => ({ ...x, read: true })));
      this.unreadCount.set(0);
    });
  }

  getNotifIcon(type: string): string {
    const map: Record<string, string> = {
      BUDGET_ALERT:    'pi-exclamation-triangle',
      GOAL_COMPLETED:  'pi-check-circle',
      GOAL_REMINDER:   'pi-flag',
      RECURRING_TX:    'pi-sync',
      WEEKLY_SUMMARY:  'pi-chart-bar',
      SYSTEM:          'pi-info-circle'
    };
    return map[type] ?? 'pi-bell';
  }

  getNotifColor(type: string): string {
    const map: Record<string, string> = {
      BUDGET_ALERT:    'notif-type-danger',
      GOAL_COMPLETED:  'notif-type-success',
      GOAL_REMINDER:   'notif-type-warning',
      RECURRING_TX:    'notif-type-info',
      WEEKLY_SUMMARY:  'notif-type-purple',
      SYSTEM:          'notif-type-default'
    };
    return map[type] ?? 'notif-type-default';
  }

  logout(): void { this.auth.logout(); }
}
