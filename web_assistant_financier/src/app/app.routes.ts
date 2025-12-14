import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./admin/components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    loadComponent: () => import('./admin/components/layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./admin/components/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'admin/users',
        loadComponent: () => import('./admin/components/users/user-list.component').then(m => m.UserListComponent)
      },
      {
        path: 'admin/courses',
        loadComponent: () => import('./admin/components/courses/course-list.component').then(m => m.CourseListComponent)
      },
      {
        path: 'admin/statistics',
        loadComponent: () => import('./admin/components/statistics/statistics.component').then(m => m.StatisticsComponent)
      },
      {
        path: 'admin/notifications',
        loadComponent: () => import('./admin/components/notifications/notifications.component').then(m => m.NotificationsComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
