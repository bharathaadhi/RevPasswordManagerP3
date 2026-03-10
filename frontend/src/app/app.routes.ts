import { Routes } from '@angular/router';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component')
        .then(m => m.LoginComponent)
  },

  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password.component')
        .then(m => m.ForgotPasswordComponent)
  },

  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component')
        .then(m => m.RegisterComponent)
  },

  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    runGuardsAndResolvers: 'always',
    children: [

      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard-home/dashboard.component')
            .then(m => m.DashboardComponent)
      },

      {
        path: 'vault',
        loadComponent: () =>
          import('./features/vault/vault-home/vault-home.component')
            .then(m => m.VaultHomeComponent)
      },

      {
        path: 'generator',
        loadComponent: () =>
          import('./features/generator/generator-home/generator.component')
            .then(m => m.GeneratorHomeComponent)
      },

      {
        path: 'security-audit',
        loadComponent: () =>
          import('./features/security/security-audit/security-audit.component')
            .then(m => m.SecurityAuditComponent)
      },

      {
        path: 'profile',
        loadComponent: () =>
          import('./features/profile/profile-home/profile.component')
            .then(m => m.ProfileHomeComponent)
      },

      {
        path: 'backup',
        loadComponent: () =>
          import('./features/backup/backup.component')
            .then(m => m.BackupComponent)
      }

    ]
  },

  {
    path: '**',
    redirectTo: 'login'
  }

];