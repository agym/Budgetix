import { Component, Output, EventEmitter, OnInit, signal, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { LanguageSwitcherComponent } from '../../shared/components/language-switcher/language-switcher.component';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [RouterLink, ButtonModule, TranslatePipe, LanguageSwitcherComponent],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss'
})
export class TopbarComponent implements OnInit {
  @Output() menuToggle = new EventEmitter<void>();

  private auth = inject(AuthService);
  user = this.auth.currentUser;
  unreadCount = signal(0);

  initials = () => {
    const name = this.user()?.name ?? 'U';
    return name.split(' ').map((n: string) => n[0]).join('').substring(0, 2).toUpperCase();
  };

  constructor(private notifications: NotificationService) {}

  ngOnInit(): void {
    this.notifications.getUnreadCount().subscribe(r => this.unreadCount.set(r.count));
  }

  logout(): void { this.auth.logout(); }
}
