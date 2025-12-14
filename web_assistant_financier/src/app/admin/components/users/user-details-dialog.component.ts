import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { UserService } from '../../../core/services/user.service';
import { User, UserDetails } from '../../../models/user.model';

@Component({
  selector: 'app-user-details-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatTabsModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatChipsModule
  ],
  templateUrl: './user-details-dialog.component.html',
  styleUrl: './user-details-dialog.component.css'
})
export class UserDetailsDialogComponent implements OnInit {
  userDetails: UserDetails | null = null;
  isLoading = true;

  budgetsColumns = ['name', 'category', 'amount', 'spent', 'period'];
  expensesColumns = ['description', 'category', 'amount', 'date'];
  progressColumns = ['course', 'completed', 'score'];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: User,
    private userService: UserService,
    private dialogRef: MatDialogRef<UserDetailsDialogComponent>
  ) {}

  ngOnInit(): void {
    this.loadUserDetails();
  }

  loadUserDetails(): void {
    this.userService.getUserById(this.data.id).subscribe({
      next: (details) => {
        this.userDetails = details;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  formatDate(date: string | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('fr-FR');
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'MAD' }).format(amount);
  }

  close(): void {
    this.dialogRef.close();
  }
}

