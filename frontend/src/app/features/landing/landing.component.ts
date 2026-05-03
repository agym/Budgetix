import { Component, HostListener, inject, OnInit, AfterViewInit, OnDestroy, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LanguageSwitcherComponent } from '../../shared/components/language-switcher/language-switcher.component';
import { LoginComponent } from '../auth/login/login.component';
import { RegisterComponent } from '../auth/register/register.component';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, TranslatePipe, LanguageSwitcherComponent, LoginComponent, RegisterComponent],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss'
})
export class LandingComponent implements OnInit, AfterViewInit, OnDestroy {
  private auth   = inject(AuthService);
  private router = inject(Router);

  scrolled  = signal(false);
  authModal = signal<null | 'login' | 'register'>(null);

  mockBars = [
    { key: 'HERO.MOCKUP_FOOD',      pct: 72, cls: 'warn',    delay: '0.9s' },
    { key: 'HERO.MOCKUP_HOUSING',   pct: 55, cls: 'primary', delay: '1.0s' },
    { key: 'HERO.MOCKUP_TRANSPORT', pct: 88, cls: 'danger',  delay: '1.1s' },
    { key: 'HERO.MOCKUP_ENTERTAIN', pct: 30, cls: 'success', delay: '1.2s' },
  ];

  stats = [
    { value: '50K+',  labelKey: 'STATS.TRANSACTIONS' },
    { value: '$2B+',  labelKey: 'STATS.WEALTH' },
    { value: '99.9%', labelKey: 'STATS.UPTIME' },
    { value: '4.9★',  labelKey: 'STATS.RATING' },
  ];

  features = [
    { icon: 'pi-chart-bar',        color: 'blue',   key: 'BUDGET' },
    { icon: 'pi-flag',             color: 'green',  key: 'GOALS' },
    { icon: 'pi-lightbulb',        color: 'purple', key: 'INSIGHTS' },
    { icon: 'pi-file-pdf',         color: 'orange', key: 'REPORTS' },
    { icon: 'pi-building-columns', color: 'cyan',   key: 'ACCOUNTS' },
    { icon: 'pi-shield',           color: 'pink',   key: 'SECURITY' },
  ];

  steps = [
    { icon: 'pi-user-plus',        key: 'STEP1' },
    { icon: 'pi-building-columns', key: 'STEP2' },
    { icon: 'pi-chart-line',       key: 'STEP3' },
  ];

  ngOnInit(): void {
    if (this.auth.isAuthenticated()) this.router.navigate(['/dashboard']);
  }

  ngAfterViewInit(): void {
    const observer = new IntersectionObserver(
      entries => entries.forEach(e => {
        if (e.isIntersecting) { e.target.classList.add('is-visible'); observer.unobserve(e.target); }
      }),
      { threshold: 0.12 }
    );
    document.querySelectorAll('.reveal, .reveal-left, .reveal-right').forEach(el => observer.observe(el));
  }

  ngOnDestroy(): void { document.body.style.overflow = ''; }

  openModal(type: 'login' | 'register'): void {
    this.authModal.set(type);
    document.body.style.overflow = 'hidden';
  }

  closeModal(): void {
    this.authModal.set(null);
    document.body.style.overflow = '';
  }

  @HostListener('window:scroll')
  onScroll(): void { this.scrolled.set(window.scrollY > 20); }

  @HostListener('keydown.escape')
  onEsc(): void { if (this.authModal() !== null) this.closeModal(); }
}
