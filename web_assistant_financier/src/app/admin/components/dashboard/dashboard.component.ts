import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { StatisticsService } from '../../../core/services/statistics.service';
import { DashboardStatistics, UserEvolution, BudgetCategoryDistribution, TopUser } from '../../../models/statistics.model';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartType, Chart, registerables } from 'chart.js';

// Enregistrer tous les composants Chart.js
Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatGridListModule,
    MatProgressSpinnerModule,
    MatIconModule,
    BaseChartDirective
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  statistics: DashboardStatistics | null = null;
  isLoading = true;

  // User Evolution Chart
  userEvolutionData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [{
      data: [],
      label: 'Utilisateurs',
      borderColor: '#6366f1',
      backgroundColor: 'rgba(99, 102, 241, 0.1)',
      borderWidth: 3,
      tension: 0.4,
      fill: true,
      pointBackgroundColor: '#6366f1',
      pointBorderColor: '#ffffff',
      pointBorderWidth: 2,
      pointRadius: 6,
      pointHoverRadius: 8,
      pointHoverBackgroundColor: '#8b5cf6',
      pointHoverBorderColor: '#ffffff',
      pointHoverBorderWidth: 3
    }]
  };
  userEvolutionOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top',
        labels: {
          usePointStyle: true,
          padding: 15,
          font: {
            size: 12
          }
        }
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        titleFont: { size: 14 },
        bodyFont: { size: 13 },
        cornerRadius: 8,
        displayColors: true
      }
    },
    scales: {
      x: {
        display: true,
        grid: {
          display: false
        },
        ticks: {
          font: { size: 11 }
        }
      },
      y: {
        beginAtZero: true,
        grid: {
          color: 'rgba(0, 0, 0, 0.05)'
        },
        ticks: {
          font: { size: 11 }
        }
      }
    }
  };

  // Budget Category Chart
  budgetCategoryData: ChartConfiguration<'pie'>['data'] = {
    labels: [],
    datasets: [{
      data: [],
      backgroundColor: [
        '#6366f1',
        '#8b5cf6',
        '#ec4899',
        '#f59e0b',
        '#10b981',
        '#3b82f6',
        '#ef4444'
      ],
      borderWidth: 3,
      borderColor: '#ffffff',
      hoverBorderWidth: 4,
      hoverOffset: 8
    }]
  };
  budgetCategoryOptions: ChartConfiguration<'pie'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          usePointStyle: true,
          padding: 15,
          font: {
            size: 12
          }
        }
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        titleFont: { size: 14 },
        bodyFont: { size: 13 },
        cornerRadius: 8,
        callbacks: {
          label: function(context) {
            const label = context.label || '';
            const value = context.parsed || 0;
            const total = context.dataset.data.reduce((a: number, b: number) => a + b, 0);
            const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
            return `${label}: ${value} (${percentage}%)`;
          }
        }
      }
    }
  };

  // Top Users Chart
  topUsersData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [{
      label: 'Points',
      data: [],
      backgroundColor: [
        '#6366f1',
        '#8b5cf6',
        '#ec4899',
        '#f59e0b',
        '#10b981'
      ],
      borderRadius: 8,
      borderSkipped: false,
      barThickness: 50
    }]
  };
  topUsersOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'x',
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        titleFont: { size: 14 },
        bodyFont: { size: 13 },
        cornerRadius: 8,
        displayColors: true
      }
    },
    scales: {
      x: {
        grid: {
          display: false
        },
        ticks: {
          font: { size: 11 }
        }
      },
      y: {
        beginAtZero: true,
        grid: {
          color: 'rgba(0, 0, 0, 0.05)'
        },
        ticks: {
          font: { size: 11 },
          callback: function(value) {
            return value + ' pts';
          }
        }
      }
    }
  };

  constructor(private statisticsService: StatisticsService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;

    // Charger les statistiques de base
    this.statisticsService.getDashboardStatistics().subscribe({
      next: (data) => {
        console.log('📊 Dashboard data received:', data);
        this.statistics = data;
        
        // Charger les données pour les graphiques depuis les endpoints séparés
        this.loadChartData();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('❌ Error loading dashboard data:', error);
        this.isLoading = false;
      }
    });
  }

  loadChartData(): void {
    // Charger l'évolution des utilisateurs
    this.statisticsService.getUserEvolution().subscribe({
      next: (data) => {
        console.log('📈 User evolution data:', data);
        if (this.statistics) {
          this.statistics.userEvolution = data;
          this.updateUserEvolutionChart(data);
        }
      },
      error: (error) => {
        console.error('❌ Error loading user evolution:', error);
      }
    });

    // Charger la distribution des budgets par catégorie
    this.statisticsService.getBudgetCategoryDistribution().subscribe({
      next: (data) => {
        console.log('🥧 Budget category data:', data);
        if (this.statistics) {
          this.statistics.budgetCategoryDistribution = data;
          this.updateBudgetCategoryChart(data);
        }
      },
      error: (error) => {
        console.error('❌ Error loading budget categories:', error);
      }
    });

    // Charger les top utilisateurs
    this.statisticsService.getTopUsers(5).subscribe({
      next: (data) => {
        console.log('👥 Top users data:', data);
        if (this.statistics) {
          this.statistics.topUsersByPoints = data;
          this.updateTopUsersChart(data);
        }
      },
      error: (error) => {
        console.error('❌ Error loading top users:', error);
      }
    });
  }

  updateUserEvolutionChart(data: any[]): void {
    if (data && Array.isArray(data) && data.length > 0) {
      this.userEvolutionData = {
        labels: data.map(item => {
          try {
            const dateStr = item.date || item;
            if (dateStr) {
              const date = new Date(dateStr);
              return date.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit' });
            }
            return '';
          } catch {
            return item.date || '';
          }
        }),
        datasets: [{
          data: data.map(item => {
            const count = typeof item === 'object' ? (item.count || item.value || 0) : (typeof item === 'number' ? item : 0);
            return typeof count === 'number' ? count : 0;
          }),
          label: 'Évolution des utilisateurs',
          borderColor: '#2563eb',
          backgroundColor: 'rgba(37, 99, 235, 0.1)',
          borderWidth: 3,
          tension: 0.4,
          fill: true,
          pointBackgroundColor: '#2563eb',
          pointBorderColor: '#ffffff',
          pointBorderWidth: 2,
          pointRadius: 6,
          pointHoverRadius: 8,
          pointHoverBackgroundColor: '#7c3aed',
          pointHoverBorderColor: '#ffffff',
          pointHoverBorderWidth: 3
        }]
      };
    }
  }

  updateBudgetCategoryChart(data: any[]): void {
    if (data && Array.isArray(data) && data.length > 0) {
      this.budgetCategoryData = {
        labels: data.map(item => {
          const category = typeof item === 'object' ? (item.category || '') : '';
          return category || 'Autre';
        }),
        datasets: [{
          data: data.map(item => {
            const amount = typeof item === 'object' ? (item.count || item.amount || item.value || 0) : (typeof item === 'number' ? item : 0);
            return typeof amount === 'number' ? amount : 0;
          }),
          backgroundColor: [
            '#2563eb',
            '#7c3aed',
            '#ec4899',
            '#f59e0b',
            '#10b981',
            '#3b82f6',
            '#ef4444'
          ],
          borderWidth: 3,
          borderColor: '#ffffff',
          hoverBorderWidth: 4,
          hoverOffset: 8
        }]
      };
    }
  }

  updateTopUsersChart(data: any[]): void {
    if (data && Array.isArray(data) && data.length > 0) {
      this.topUsersData = {
        labels: data.map(item => {
          const username = typeof item === 'object' ? (item.username || '') : '';
          return username || 'Utilisateur';
        }),
        datasets: [{
          label: 'Points',
          data: data.map(item => {
            const points = typeof item === 'object' ? (item.points || item.value || 0) : (typeof item === 'number' ? item : 0);
            return typeof points === 'number' ? points : 0;
          }),
          backgroundColor: [
            '#2563eb',
            '#7c3aed',
            '#ec4899',
            '#f59e0b',
            '#10b981'
          ],
          borderRadius: 8,
          borderSkipped: false,
          barThickness: 50
        }]
      };
    }
  }

  updateCharts(data: DashboardStatistics): void {
    // Cette méthode est maintenant remplacée par les méthodes spécifiques
    // updateUserEvolutionChart, updateBudgetCategoryChart, updateTopUsersChart
  }
}

