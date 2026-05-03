import { Injectable, NgZone, OnDestroy, inject } from '@angular/core';
import { Subject, Subscription, interval } from 'rxjs';

const TIMEOUT_MS      = 15 * 60 * 1000;  // 15 min → logout
const WARNING_MS      = 13 * 60 * 1000;  // 13 min → show warning
const TICK_INTERVAL   = 1000;
const ACTIVITY_EVENTS = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart', 'wheel'];

@Injectable({ providedIn: 'root' })
export class InactivityService implements OnDestroy {
  readonly warn$   = new Subject<number>();  // emits countdown seconds
  readonly logout$ = new Subject<void>();

  private lastActivity = Date.now();
  private tickSub?: Subscription;
  private listeners: Array<() => void> = [];
  private zone = inject(NgZone);
  private warned = false;

  start(): void {
    this.lastActivity = Date.now();
    this.warned = false;

    // Run event listeners outside Angular zone to avoid CD on every mouse move
    this.zone.runOutsideAngular(() => {
      ACTIVITY_EVENTS.forEach(evt => {
        const handler = () => this.onActivity();
        window.addEventListener(evt, handler, { passive: true });
        this.listeners.push(() => window.removeEventListener(evt, handler));
      });
    });

    this.tickSub = interval(TICK_INTERVAL).subscribe(() => this.check());
  }

  stop(): void {
    this.tickSub?.unsubscribe();
    this.listeners.forEach(remove => remove());
    this.listeners = [];
    this.warned = false;
  }

  reset(): void {
    this.lastActivity = Date.now();
    this.warned = false;
  }

  private onActivity(): void {
    this.lastActivity = Date.now();
    if (this.warned) {
      this.zone.run(() => { this.warned = false; });
    }
  }

  private check(): void {
    const idle = Date.now() - this.lastActivity;

    if (idle >= TIMEOUT_MS) {
      this.zone.run(() => this.logout$.next());
      this.stop();
      return;
    }

    if (idle >= WARNING_MS && !this.warned) {
      this.warned = true;
      const remaining = Math.ceil((TIMEOUT_MS - idle) / 1000);
      this.zone.run(() => this.warn$.next(remaining));
    }

    if (this.warned) {
      const remaining = Math.ceil((TIMEOUT_MS - idle) / 1000);
      this.zone.run(() => this.warn$.next(remaining));
    }
  }

  ngOnDestroy(): void {
    this.stop();
  }
}
