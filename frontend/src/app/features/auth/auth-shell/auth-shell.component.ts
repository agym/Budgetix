import { Component, inject, computed } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd } from '@angular/router';
import { filter, map } from 'rxjs/operators';
import { LanguageSwitcherComponent } from '../../../shared/components/language-switcher/language-switcher.component';

@Component({
  selector: 'app-auth-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslatePipe, LanguageSwitcherComponent],
  templateUrl: './auth-shell.component.html',
  styleUrl: './auth-shell.component.scss'
})
export class AuthShellComponent {
  private router = inject(Router);

  private url = toSignal(
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      map((e: NavigationEnd) => e.urlAfterRedirects)
    ),
    { initialValue: this.router.url }
  );

  showTabs = computed(() => {
    const u = this.url();
    return u.includes('/auth/login') || u.includes('/auth/register');
  });
}
