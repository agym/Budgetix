import { Injectable, signal, effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly isDark = signal<boolean>(this.loadPreference());

  constructor() {
    this.apply(this.isDark());
    effect(() => {
      const dark = this.isDark();
      this.apply(dark);
      localStorage.setItem('budgetix-theme', dark ? 'dark' : 'light');
    });
  }

  toggle(): void {
    this.isDark.update(v => !v);
  }

  private apply(dark: boolean): void {
    document.documentElement.classList.toggle('dark', dark);
  }

  private loadPreference(): boolean {
    const saved = localStorage.getItem('budgetix-theme');
    if (saved) return saved === 'dark';
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }
}
