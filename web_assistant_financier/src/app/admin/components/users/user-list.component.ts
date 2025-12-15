import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../models/user.model';
import { UserDetailsDialogComponent } from './user-details-dialog.component';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css'
})
export class UserListComponent implements OnInit {
  displayedColumns: string[] = ['id', 'username', 'email', 'createdAt', 'status', 'actions'];
  users: User[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  isLoading = false;
  searchControl = new FormControl('');

  constructor(
    private userService: UserService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadUsers();

    this.searchControl.valueChanges
      .pipe(debounceTime(500), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex = 0;
        this.loadUsers();
      });
  }

  loadUsers(): void {
    this.isLoading = true;
    const search = this.searchControl.value || undefined;

    this.userService.getAllUsers(this.pageIndex, this.pageSize, search).subscribe({
      next: (response) => {
        this.users = response.content;
        this.totalElements = response.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des utilisateurs:', error);
        this.isLoading = false;
        const errorMsg = error.error?.message || error.message || 'Erreur lors du chargement des utilisateurs';
        this.snackBar.open(errorMsg, 'Fermer', { duration: 3000 });
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  viewDetails(user: User): void {
    this.dialog.open(UserDetailsDialogComponent, {
      width: '800px',
      data: user
    });
  }

  toggleStatus(user: User): void {
    const action = user.enabled ? 'désactiver' : 'activer';
    if (confirm(`Êtes-vous sûr de vouloir ${action} l'utilisateur ${user.username} ?`)) {
      this.userService.toggleUserStatus(user.id).subscribe({
        next: () => {
          this.snackBar.open(`Utilisateur ${user.enabled ? 'désactivé' : 'activé'} avec succès`, 'Fermer', { duration: 3000 });
          this.loadUsers();
        },
        error: (error) => {
          console.error('Erreur lors du changement de statut:', error);
          const errorMsg = error.error?.message || error.message || 'Erreur lors de la modification';
          this.snackBar.open(errorMsg, 'Fermer', { duration: 5000 });
        }
      });
    }
  }

  deleteUser(user: User): void {
    if (confirm(`Êtes-vous sûr de vouloir supprimer l'utilisateur "${user.username}" ?\n\nCette action est irréversible.`)) {
      console.log('🗑️ Tentative de suppression de l\'utilisateur:', user.id, user.username);
      this.userService.deleteUser(user.id).subscribe({
        next: () => {
          console.log('✅ Utilisateur supprimé avec succès');
          this.snackBar.open('Utilisateur supprimé avec succès', 'Fermer', { duration: 3000 });
          // Ajuster la page si nécessaire après suppression
          if (this.users.length === 1 && this.pageIndex > 0) {
            this.pageIndex--;
          }
          this.loadUsers();
        },
        error: (error) => {
          console.error('❌ Erreur lors de la suppression:', error);
          console.error('❌ Status:', error.status);
          console.error('❌ Error object:', error.error);
          
          let errorMsg = 'Erreur lors de la suppression';
          
          if (error.status === 404) {
            errorMsg = error.error?.message || error.error || 'Utilisateur non trouvé';
          } else if (error.status === 500) {
            errorMsg = error.error?.message || error.error || 'Erreur serveur lors de la suppression';
          } else if (error.error) {
            if (typeof error.error === 'string') {
              errorMsg = error.error;
            } else if (error.error.message) {
              errorMsg = error.error.message;
            }
          } else if (error.message) {
            errorMsg = error.message;
          }
          
          this.snackBar.open(errorMsg, 'Fermer', { duration: 5000 });
        }
      });
    }
  }

  formatDate(date: string | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('fr-FR');
  }
}

