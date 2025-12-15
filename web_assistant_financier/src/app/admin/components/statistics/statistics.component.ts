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
      backgroundColor: '#2563eb',
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
        console.log('📊 Course statistics received:', stats);
        // Convertir les données du backend au format attendu
        this.courseStatistics = stats.map((s: any) => ({
          courseId: s.courseId || s.id,
          courseTitle: s.courseTitle || s.title || 'Cours inconnu',
          completions: s.completions || s.completionCount || 0,
          averageScore: s.averageScore || 0
        }));
        
        if (this.courseStatistics.length > 0) {
          this.courseStatsData = {
            labels: this.courseStatistics.map(s => s.courseTitle),
            datasets: [{
              label: 'Complétions',
              data: this.courseStatistics.map(s => s.completions),
              backgroundColor: '#2563eb',
              borderRadius: 8,
              borderSkipped: false,
              barThickness: 40
            }, {
              label: 'Score moyen (%)',
              data: this.courseStatistics.map(s => s.averageScore),
              backgroundColor: '#7c3aed',
              borderRadius: 8,
              borderSkipped: false,
              barThickness: 40,
              yAxisID: 'y1'
            }]
          };
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('❌ Error loading course statistics:', error);
        this.isLoading = false;
      }
    });
  }

  exportData(format: 'csv' | 'pdf'): void {
    console.log(`📥 Tentative d'export ${format.toUpperCase()}...`);
    this.statisticsService.exportData(format).subscribe({
      next: (blob) => {
        console.log('✅ Fichier reçu, taille:', blob.size, 'bytes');
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        
        // Générer un nom de fichier avec timestamp
        const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, -5);
        a.download = `statistics_${timestamp}.${format}`;
        
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        
        console.log(`✅ Export ${format.toUpperCase()} réussi`);
      },
      error: (error) => {
        console.error(`❌ Erreur lors de l'export ${format}:`, error);
        let errorMsg = `Erreur lors de l'export ${format.toUpperCase()}`;
        
        if (error.status === 400) {
          errorMsg = error.error || 'Format non supporté';
        } else if (error.status === 500) {
          errorMsg = error.error || 'Erreur serveur lors de l\'export';
        } else if (error.error) {
          if (typeof error.error === 'string') {
            errorMsg = error.error;
          } else if (error.error.message) {
            errorMsg = error.error.message;
          }
        }
        
        alert(errorMsg);
      }
    });
  }
}

