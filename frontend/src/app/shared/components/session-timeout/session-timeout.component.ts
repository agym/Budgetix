import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { InactivityService } from '../../../core/services/inactivity.service';
import { AuthService } from '../../../core/services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-session-timeout',
  standalone: true,
  imports: [TranslatePipe],
  templateUrl: './session-timeout.component.html',
  styleUrl: './session-timeout.component.scss'
})
export class SessionTimeoutComponent implements OnInit, OnDestroy {
  private inactivity = inject(InactivityService);
  private auth       = inject(AuthService);

  visible   = signal(false);
  countdown = signal(120);

  private readonly MAX_COUNTDOWN = 120;
  private subs: Subscription[] = [];

  ngOnInit(): void {
    this.subs.push(
      this.inactivity.warn$.subscribe(secs => {
        this.visible.set(true);
        this.countdown.set(secs);
      }),
      this.inactivity.logout$.subscribe(() => {
        this.visible.set(false);
        this.auth.logout();
      })
    );
  }

  progressPct(): number {
    return (this.countdown() / this.MAX_COUNTDOWN) * 100;
  }

  continueSession(): void {
    this.inactivity.reset();
    this.visible.set(false);
  }

  signOut(): void {
    this.visible.set(false);
    this.inactivity.stop();
    this.auth.logout();
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }
}
