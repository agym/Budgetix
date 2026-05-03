import { Component, inject, signal, HostListener } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

interface Lang { code: string; label: string; flag: string; dir: 'ltr' | 'rtl'; }

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [],
  templateUrl: './language-switcher.component.html',
  styleUrl: './language-switcher.component.scss'
})
export class LanguageSwitcherComponent {
  private translate = inject(TranslateService);

  open = signal(false);

  langs: Lang[] = [
    { code: 'en', label: 'English',  flag: '🇬🇧', dir: 'ltr' },
    { code: 'fr', label: 'Français', flag: '🇫🇷', dir: 'ltr' },
    { code: 'ar', label: 'العربية',  flag: '🇸🇦', dir: 'rtl' },
  ];

  current = signal(this.langs.find(l => l.code === (localStorage.getItem('budgetix_lang') ?? 'en')) ?? this.langs[0]);

  select(lang: Lang): void {
    this.translate.use(lang.code);
    this.current.set(lang);
    document.documentElement.lang = lang.code;
    document.documentElement.dir  = lang.dir;
    localStorage.setItem('budgetix_lang', lang.code);
    this.open.set(false);
  }

  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent): void {
    if (!(e.target as HTMLElement).closest('.lang-wrapper')) {
      this.open.set(false);
    }
  }
}
