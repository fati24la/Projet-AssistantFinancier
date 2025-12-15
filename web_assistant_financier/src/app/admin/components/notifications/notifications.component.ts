import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { NotificationService, NotificationRequest } from '../../../core/services/notification.service';
import { NotificationHistory } from '../../../models/statistics.model';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule,
    MatChipsModule
  ],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.css'
})
export class NotificationsComponent implements OnInit {
  notificationForm: FormGroup;
  history: NotificationHistory[] = [];
  isLoading = false;
  isSending = false;

  displayedColumns = ['title', 'message', 'sentAt', 'recipientsCount'];

  templates = [
    { title: 'Bienvenue', message: 'Bienvenue dans l\'Assistant Financier ! Commencez votre parcours d\'apprentissage dès aujourd\'hui.' },
    { title: 'Nouveau guide', message: 'Un nouveau guide est maintenant disponible ! Découvrez-le dans la section Guides.' },
    { title: 'Rappel budget', message: 'N\'oubliez pas de suivre vos budgets et vos dépenses régulièrement.' }
  ];

  constructor(
    private fb: FormBuilder,
    private notificationService: NotificationService,
    private snackBar: MatSnackBar
  ) {
    this.notificationForm = this.fb.group({
      title: ['', Validators.required],
      message: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadHistory();
  }

  loadHistory(): void {
    this.isLoading = true;
    this.notificationService.getNotificationHistory().subscribe({
      next: (history) => {
        this.history = history;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  useTemplate(template: { title: string; message: string }): void {
    this.notificationForm.patchValue({
      title: template.title,
      message: template.message
    });
  }

  sendNotification(): void {
    if (this.notificationForm.valid) {
      this.isSending = true;
      const request: NotificationRequest = this.notificationForm.value;

      this.notificationService.broadcastNotification(request).subscribe({
        next: (response) => {
          this.snackBar.open(
            `Notification envoyée à ${response.recipientsCount} utilisateurs`,
            'Fermer',
            { duration: 5000 }
          );
          this.notificationForm.reset();
          this.isSending = false;
          this.loadHistory();
        },
        error: () => {
          this.isSending = false;
          this.snackBar.open('Erreur lors de l\'envoi', 'Fermer', { duration: 3000 });
        }
      });
    }
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleString('fr-FR');
  }
}

