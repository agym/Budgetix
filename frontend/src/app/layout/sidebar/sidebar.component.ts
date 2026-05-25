import { Component, Input, Output, EventEmitter } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

interface NavGroup {
  heading?: string;
  items: NavItem[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Output() toggle = new EventEmitter<void>();

  navGroups: NavGroup[] = [
    {
      heading: 'Overview',
      items: [
        { label: 'Dashboard',    icon: 'pi-home',             route: '/dashboard' },
        { label: 'Transactions', icon: 'pi-credit-card',      route: '/transactions' },
      ]
    },
    {
      heading: 'Finance',
      items: [
        { label: 'Accounts',     icon: 'pi-building-columns', route: '/accounts'    },
        { label: 'Budgets',      icon: 'pi-chart-bar',        route: '/budgets'     },
        { label: 'Goals',        icon: 'pi-flag',             route: '/goals'       },
        { label: 'Recurring',    icon: 'pi-sync',             route: '/recurring'   },
        { label: 'Categories',   icon: 'pi-tag',              route: '/categories'  },
      ]
    },
    {
      heading: 'Insights',
      items: [
        { label: 'Calendar',     icon: 'pi-calendar',         route: '/calendar'    },
        { label: 'Insights',     icon: 'pi-lightbulb',        route: '/insights'    },
        { label: 'Reports',      icon: 'pi-file-pdf',         route: '/reports'     },
      ]
    },
    {
      heading: 'Account',
      items: [
        { label: 'Settings',     icon: 'pi-cog',              route: '/settings' },
      ]
    }
  ];
}
