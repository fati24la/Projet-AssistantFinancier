import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { StatisticsService } from '../../../core/services/statistics.service';
import { CourseStatistics } from '../../../models/statistics.model';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
    BaseChartDirective
  ],
  templateUrl: './statistics.component.html',
  styleUrl: './statistics.component.css'
})
export class StatisticsComponent implements OnInit {
  isLoading = true;
  courseStatistics: CourseStatistics[] = [];

  courseStatsData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [{
      label: 'Complétions',
      data: [],
      backgroundColor: '#6366f1',
      borderRadius: 8,
      borderSkipped: false,
      barThickness: 40
    }, {
      label: 'Score moyen (%)',
      data: [],
      backgroundColor: '#8b5cf6',
      borderRadius: 8,
      borderSkipped: false,
      barThickness: 40,
      yAxisID: 'y1'
    }]
  };

  courseStatsOptions: ChartConfiguration<'bar'>['options'] = {
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
            size: 12,
            weight: 'normal'
          }
        }
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        titleFont: { size: 14, weight: 'bold' },
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
          font: { size: 11 }
        }
      },
      y1: {
        type: 'linear',
        display: true,
        position: 'right',
        beginAtZero: true,
        max: 100,
        grid: {
          display: false
        },
        ticks: {
          font: { size: 11 },
          callback: function(value) {
            return value + '%';
          }
        }
      }
    }
  };

  constructor(private statisticsService: StatisticsService) {}

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.isLoading = true;
    this.statisticsService.getCourseStatistics().subscribe({
      next: (stats) => {
        this.courseStatistics = stats;
        this.courseStatsData.labels = stats.map(s => s.courseTitle);
        this.courseStatsData.datasets[0].data = stats.map(s => s.completions);
        this.courseStatsData.datasets[1].data = stats.map(s => s.averageScore);
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  exportData(format: 'csv' | 'pdf'): void {
    this.statisticsService.exportData(format).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `statistics.${format}`;
        a.click();
        window.URL.revokeObjectURL(url);
      }
    });
  }
}

