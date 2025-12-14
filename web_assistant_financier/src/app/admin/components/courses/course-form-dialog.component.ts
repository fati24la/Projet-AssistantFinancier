import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { CourseService } from '../../../core/services/course.service';
import { Course, CourseDto } from '../../../models/course.model';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-course-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './course-form-dialog.component.html',
  styleUrl: './course-form-dialog.component.css'
})
export class CourseFormDialogComponent implements OnInit {
  courseForm: FormGroup;
  isEditMode = false;
  isLoading = false;

  categories = ['BUDGETING', 'SAVINGS', 'CREDIT', 'INSURANCE', 'INCLUSION'];
  difficulties = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
  languages = ['FR', 'AR', 'AMZ'];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: Course | null,
    private fb: FormBuilder,
    private courseService: CourseService,
    private dialogRef: MatDialogRef<CourseFormDialogComponent>,
    private snackBar: MatSnackBar
  ) {
    this.isEditMode = !!data;
    this.courseForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      content: ['', Validators.required],
      category: ['', Validators.required],
      difficulty: ['', Validators.required],
      durationMinutes: [0, [Validators.required, Validators.min(1)]],
      language: ['', Validators.required],
      isActive: [true]
    });
  }

  ngOnInit(): void {
    if (this.data) {
      this.courseForm.patchValue(this.data);
    }
  }

  onSubmit(): void {
    if (this.courseForm.valid) {
      this.isLoading = true;
      const courseDto: CourseDto = this.courseForm.value;

      const operation = this.isEditMode
        ? this.courseService.updateCourse(this.data!.id, courseDto)
        : this.courseService.createCourse(courseDto);

      operation.subscribe({
        next: () => {
          this.snackBar.open(
            `Cours ${this.isEditMode ? 'modifié' : 'créé'} avec succès`,
            'Fermer',
            { duration: 3000 }
          );
          this.dialogRef.close(true);
        },
        error: () => {
          this.isLoading = false;
          this.snackBar.open('Erreur lors de l\'opération', 'Fermer', { duration: 3000 });
        }
      });
    }
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

