import { Component, OnDestroy, OnInit, signal, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';
import { SessionTimeoutComponent } from '../../shared/components/session-timeout/session-timeout.component';
import { InactivityService } from '../../core/services/inactivity.service';
import { AuthService } from '../../core/services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, TopbarComponent, SessionTimeoutComponent],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent implements OnInit, OnDestroy {
  collapsed = signal(false);

  private inactivity = inject(InactivityService);
  private auth       = inject(AuthService);
  private sub?: Subscription;

  ngOnInit(): void {
    this.inactivity.start();
    this.sub = this.inactivity.logout$.subscribe(() => this.auth.logout());
  }

  ngOnDestroy(): void {
    this.inactivity.stop();
    this.sub?.unsubscribe();
  }
}
