import { Component, OnInit, signal, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TabViewModule } from 'primeng/tabview';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { ToggleButtonModule } from 'primeng/togglebutton';
import { ToastModule } from 'primeng/toast';
import { DividerModule } from 'primeng/divider';
import { AvatarModule } from 'primeng/avatar';
import { MessageService } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { NotificationService, Notification } from '../../core/services/notification.service';
import { UserService, UserProfile } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    DatePipe, FormsModule, ReactiveFormsModule,
    TabViewModule, InputTextModule, ButtonModule, ToggleButtonModule,
    ToastModule, DividerModule, AvatarModule, TableModule, TagModule
  ],
  providers: [MessageService],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss'
})
export class SettingsComponent implements OnInit {
  private auth = inject(AuthService);
  user = this.auth.currentUser;
  twoFactorEnabled = signal(false);
  savingProfile = signal(false);
  savingPassword = signal(false);
  toggling2FA = signal(false);
  savingNotif = signal(false);
  notifications = signal<Notification[]>([]);

  profileForm: FormGroup;
  passwordForm: FormGroup;

  notifPrefs = [
    { key: 'notifyBudgetAlerts', label: 'Budget Alerts', description: 'Get notified when you approach or exceed a budget limit', value: true },
    { key: 'notifyGoalMilestones', label: 'Goal Milestones', description: 'Notifications when you reach savings goal milestones', value: true },
    { key: 'notifyWeeklySummary', label: 'Weekly Summary', description: 'Receive a weekly financial summary email', value: false },
    { key: 'notifyLargeTransactions', label: 'Large Transactions', description: 'Alert when a transaction exceeds your set threshold', value: true },
  ];

  initials = () => {
    const name = this.user()?.name ?? 'U';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private notifService: NotificationService,
    private toast: MessageService
  ) {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      currency: ['USD', [Validators.required, Validators.maxLength(3)]],
      monthlyIncome: [0],
      timezone: ['UTC']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.loadProfile();
    this.loadNotifications();
  }

  private loadProfile(): void {
    this.userService.getMe().subscribe(data => {
      this.profileForm.patchValue({
        name: data.name,
        currency: data.profile?.currency ?? 'USD',
        monthlyIncome: data.profile?.monthlyIncome ?? 0,
        timezone: data.profile?.timezone ?? 'UTC'
      });
      this.twoFactorEnabled.set(data.twoFactorEnabled ?? false);
      const notifMap: Record<string, boolean> = {
        notifyBudgetAlerts: data.profile?.notifyBudgetAlerts ?? true,
        notifyGoalMilestones: data.profile?.notifyGoalMilestones ?? true,
        notifyWeeklySummary: data.profile?.notifyWeeklySummary ?? false,
        notifyLargeTransactions: data.profile?.notifyLargeTransactions ?? true,
      };
      this.notifPrefs.forEach(p => p.value = notifMap[p.key] ?? p.value);
    });
  }

  private loadNotifications(): void {
    this.notifService.getAll(false, 0, 20).subscribe(page => this.notifications.set(page.content));
  }

  saveProfile(): void {
    if (this.profileForm.invalid) return;
    this.savingProfile.set(true);
    const { name, ...settings } = this.profileForm.value;
    this.userService.updateProfile({ name }).subscribe({
      next: () => {
        this.userService.updateSettings(settings).subscribe({
          next: () => {
            this.toast.add({ severity: 'success', summary: 'Profile saved' });
            this.savingProfile.set(false);
          },
          error: () => { this.toast.add({ severity: 'error', summary: 'Failed to save settings' }); this.savingProfile.set(false); }
        });
      },
      error: () => { this.toast.add({ severity: 'error', summary: 'Failed to save profile' }); this.savingProfile.set(false); }
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid || this.passwordForm.errors?.['mismatch']) return;
    this.savingPassword.set(true);
    const { currentPassword, newPassword } = this.passwordForm.value;
    this.userService.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Password changed successfully' });
        this.passwordForm.reset();
        this.savingPassword.set(false);
      },
      error: () => { this.toast.add({ severity: 'error', summary: 'Failed to change password' }); this.savingPassword.set(false); }
    });
  }

  toggleTwoFactor(): void {
    this.toggling2FA.set(true);
    this.userService.enableTwoFactor(!this.twoFactorEnabled()).subscribe({
      next: () => {
        this.twoFactorEnabled.update(v => !v);
        this.toast.add({ severity: 'success', summary: `2FA ${this.twoFactorEnabled() ? 'enabled' : 'disabled'}` });
        this.toggling2FA.set(false);
      },
      error: () => { this.toast.add({ severity: 'error', summary: 'Failed to update 2FA' }); this.toggling2FA.set(false); }
    });
  }

  saveNotifPrefs(): void {
    this.savingNotif.set(true);
    const prefs: Record<string, boolean> = {};
    this.notifPrefs.forEach(p => prefs[p.key] = p.value);
    this.userService.updateSettings(prefs).subscribe({
      next: () => { this.toast.add({ severity: 'success', summary: 'Preferences saved' }); this.savingNotif.set(false); },
      error: () => { this.toast.add({ severity: 'error', summary: 'Failed to save preferences' }); this.savingNotif.set(false); }
    });
  }

  markRead(n: Notification): void {
    this.notifService.markRead(n.id).subscribe(() => {
      this.notifications.update(list => list.map(x => x.id === n.id ? { ...x, read: true } : x));
    });
  }

  markAllRead(): void {
    this.notifService.markAllRead().subscribe(() => {
      this.notifications.update(list => list.map(x => ({ ...x, read: true })));
    });
  }

  deleteNotif(n: Notification): void {
    this.notifService.remove(n.id).subscribe(() => {
      this.notifications.update(list => list.filter(x => x.id !== n.id));
    });
  }

  getNotifIcon(type: string): string {
    const map: Record<string, string> = {
      BUDGET_ALERT: 'pi-exclamation-triangle',
      GOAL_COMPLETED: 'pi-flag',
      RECURRING_TRANSACTION: 'pi-sync',
      WEEKLY_SUMMARY: 'pi-chart-bar'
    };
    return map[type] ?? 'pi-bell';
  }

  private passwordMatchValidator(form: FormGroup) {
    const pw = form.get('newPassword')?.value;
    const confirm = form.get('confirmPassword')?.value;
    return pw && confirm && pw !== confirm ? { mismatch: true } : null;
  }
}
