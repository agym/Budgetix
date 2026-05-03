import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet />`
})
export class AppComponent implements OnInit {
  private translate = inject(TranslateService);

  ngOnInit(): void {
    const saved = localStorage.getItem('budgetix_lang') ?? 'en';
    const dir   = saved === 'ar' ? 'rtl' : 'ltr';
    this.translate.setDefaultLang('en');
    this.translate.addLangs(['en', 'fr', 'ar']);
    this.translate.use(saved).subscribe();
    document.documentElement.lang = saved;
    document.documentElement.dir  = dir;
  }
}
