import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { CourseService } from '../../../core/services/course.service';
import { Course } from '../../../models/course.model';
import { CourseFormDialogComponent } from './course-form-dialog.component';
import { QuizManagementComponent } from './quiz-management.component';
import { ConfirmDialogComponent } from '../shared/confirm-dialog.component';

@Component({
  selector: 'app-course-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './course-list.component.html',
  styleUrl: './course-list.component.css'
})
export class CourseListComponent implements OnInit {
  displayedColumns: string[] = ['title', 'category', 'language', 'difficulty', 'duration', 'status', 'actions'];
  courses: Course[] = [];
  isLoading = false;

  categoryFilter = new FormControl('');
  languageFilter = new FormControl('');
  statusFilter = new FormControl('');

  categories = ['BUDGETING', 'SAVINGS', 'CREDIT', 'INSURANCE', 'INCLUSION'];
  languages = ['FR', 'AR', 'AMZ'];

  constructor(
    private courseService: CourseService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCourses();

    this.categoryFilter.valueChanges.subscribe(() => this.loadCourses());
    this.languageFilter.valueChanges.subscribe(() => this.loadCourses());
    this.statusFilter.valueChanges.subscribe(() => this.loadCourses());
  }

  loadCourses(): void {
    this.isLoading = true;
    const category = this.categoryFilter.value || undefined;
    const language = this.languageFilter.value || undefined;
    const isActive = this.statusFilter.value ? this.statusFilter.value === 'true' : undefined;

    this.courseService.getCourses(category, language, isActive).subscribe({
      next: (courses) => {
        this.courses = courses;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Erreur lors du chargement des cours', 'Fermer', { duration: 3000 });
      }
    });
  }

  createCourse(): void {
    const dialogRef = this.dialog.open(CourseFormDialogComponent, {
      width: '800px',
      data: null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadCourses();
      }
    });
  }

  editCourse(course: Course): void {
    const dialogRef = this.dialog.open(CourseFormDialogComponent, {
      width: '800px',
      data: course
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadCourses();
      }
    });
  }

  toggleStatus(course: Course): void {
    console.log('🔄 Tentative de changement de statut pour le cours:', course.id, course.title);
    this.courseService.toggleCourseStatus(course.id).subscribe({
      next: () => {
        console.log('✅ Statut du cours modifié avec succès');
        this.snackBar.open('Statut du cours modifié', 'Fermer', { duration: 3000 });
        this.loadCourses();
      },
      error: (error) => {
        console.error('❌ Erreur lors de la modification:', error);
        console.error('❌ Status:', error.status);
        console.error('❌ Error object:', error.error);
        
        let errorMsg = 'Erreur lors de la modification';
        
        if (error.status === 404) {
          errorMsg = error.error?.message || error.error || 'Cours non trouvé';
        } else if (error.status === 500) {
          errorMsg = error.error?.message || error.error || 'Erreur serveur lors de la modification';
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

  deleteCourse(course: Course): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Supprimer le guide',
        message: `Êtes-vous sûr de vouloir supprimer le cours "${course.title}" ?\nCette action est irréversible.`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler',
        icon: 'delete',
        color: 'warn'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log('🗑️ Tentative de suppression du cours:', course.id, course.title);
        this.courseService.deleteCourse(course.id).subscribe({
          next: () => {
            console.log('✅ Cours supprimé avec succès');
            this.snackBar.open('Cours supprimé avec succès', 'Fermer', { duration: 3000 });
            this.loadCourses();
          },
          error: (error) => {
            console.error('❌ Erreur lors de la suppression:', error);
            console.error('❌ Status:', error.status);
            console.error('❌ Error object:', error.error);
            
            let errorMsg = 'Erreur lors de la suppression';
            
            if (error.status === 404) {
              errorMsg = error.error?.message || error.error || 'Cours non trouvé';
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
    });
  }

  viewQuizzes(course: Course): void {
    const dialogRef = this.dialog.open(QuizManagementComponent, {
      width: '900px',
      maxWidth: '95vw',
      maxHeight: '90vh',
      data: course
    });

    dialogRef.afterClosed().subscribe(() => {
      this.loadCourses();
    });
  }
}

