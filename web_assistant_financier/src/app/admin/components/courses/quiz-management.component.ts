import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CourseService } from '../../../core/services/course.service';
import { Course, Quiz } from '../../../models/course.model';
import { ConfirmDialogComponent } from '../shared/confirm-dialog.component';

@Component({
  selector: 'app-quiz-management',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTooltipModule
  ],
  templateUrl: './quiz-management.component.html',
  styleUrl: './quiz-management.component.css'
})
export class QuizManagementComponent implements OnInit {
  course: Course;
  quizzes: Quiz[] = [];
  isLoading = false;
  displayedColumns = ['question', 'options', 'correctAnswer', 'actions'];
  quizForm: FormGroup;
  editingQuizId: number | null = null;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: Course,
    private courseService: CourseService,
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<QuizManagementComponent>,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.course = data;
    this.quizForm = this.fb.group({
      question: ['', Validators.required],
      options: this.fb.array([
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required)
      ]),
      correctAnswerIndex: [0, [Validators.required, Validators.min(0)]],
      explanation: ['']
    });
  }

  ngOnInit(): void {
    this.loadQuizzes();
  }

  get optionsArray(): FormArray {
    return this.quizForm.get('options') as FormArray;
  }

  loadQuizzes(): void {
    this.isLoading = true;
    this.courseService.getCourseById(this.course.id).subscribe({
      next: (course) => {
        this.quizzes = course.quizzes || [];
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Erreur lors du chargement des quiz', 'Fermer', { duration: 3000 });
      }
    });
  }

  addOption(): void {
    this.optionsArray.push(this.fb.control('', Validators.required));
  }

  removeOption(index: number): void {
    if (this.optionsArray.length > 2) {
      this.optionsArray.removeAt(index);
      if (this.quizForm.get('correctAnswerIndex')?.value >= this.optionsArray.length) {
        this.quizForm.patchValue({ correctAnswerIndex: this.optionsArray.length - 1 });
      }
    }
  }

  startEdit(quiz: Quiz): void {
    this.editingQuizId = quiz.id || null;
    this.quizForm.patchValue({
      question: quiz.question,
      correctAnswerIndex: quiz.correctAnswerIndex,
      explanation: quiz.explanation || ''
    });

    // Réinitialiser les options
    while (this.optionsArray.length > 0) {
      this.optionsArray.removeAt(0);
    }
    quiz.options.forEach(option => {
      this.optionsArray.push(this.fb.control(option, Validators.required));
    });
  }

  cancelEdit(): void {
    this.editingQuizId = null;
    this.quizForm.reset();
    this.quizForm.patchValue({ correctAnswerIndex: 0 });
    while (this.optionsArray.length > 4) {
      this.optionsArray.removeAt(this.optionsArray.length - 1);
    }
    while (this.optionsArray.length < 4) {
      this.optionsArray.push(this.fb.control('', Validators.required));
    }
  }

  saveQuiz(): void {
    if (this.quizForm.valid) {
      const quizData: Quiz = {
        question: this.quizForm.value.question,
        options: this.optionsArray.value.filter((opt: string) => opt.trim() !== ''),
        correctAnswerIndex: this.quizForm.value.correctAnswerIndex,
        explanation: this.quizForm.value.explanation,
        courseId: this.course.id
      };

      if (quizData.options.length < 2) {
        this.snackBar.open('Au moins 2 options sont requises', 'Fermer', { duration: 3000 });
        return;
      }

      if (quizData.correctAnswerIndex! >= quizData.options.length) {
        this.snackBar.open('L\'index de la réponse correcte est invalide', 'Fermer', { duration: 3000 });
        return;
      }

      this.isLoading = true;

      if (this.editingQuizId) {
        this.courseService.updateQuiz(this.course.id, this.editingQuizId, quizData).subscribe({
          next: () => {
            this.snackBar.open('Quiz modifié avec succès', 'Fermer', { duration: 3000 });
            this.cancelEdit();
            this.loadQuizzes();
          },
          error: () => {
            this.isLoading = false;
            this.snackBar.open('Erreur lors de la modification', 'Fermer', { duration: 3000 });
          }
        });
      } else {
        this.courseService.addQuizToCourse(this.course.id, quizData).subscribe({
          next: () => {
            this.snackBar.open('Quiz ajouté avec succès', 'Fermer', { duration: 3000 });
            this.cancelEdit();
            this.loadQuizzes();
          },
          error: () => {
            this.isLoading = false;
            this.snackBar.open('Erreur lors de l\'ajout', 'Fermer', { duration: 3000 });
          }
        });
      }
    }
  }

  deleteQuiz(quizId: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Supprimer le quiz',
        message: 'Êtes-vous sûr de vouloir supprimer ce quiz ?',
        confirmText: 'Supprimer',
        cancelText: 'Annuler',
        icon: 'quiz',
        color: 'warn'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.courseService.deleteQuiz(this.course.id, quizId).subscribe({
          next: () => {
            this.snackBar.open('Quiz supprimé', 'Fermer', { duration: 3000 });
            this.loadQuizzes();
          },
          error: () => {
            this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
          }
        });
      }
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}

