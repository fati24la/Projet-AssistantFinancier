import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Course, CourseDto, Quiz } from '../../models/course.model';

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private readonly API_URL = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getCourses(category?: string, language?: string, isActive?: boolean): Observable<Course[]> {
    let params = new HttpParams();
    if (category) params = params.set('category', category);
    if (language) params = params.set('language', language);
    if (isActive !== undefined) params = params.set('isActive', isActive.toString());

    return this.http.get<Course[]>(`${this.API_URL}/admin/courses`, { params });
  }

  getCourseById(id: number): Observable<Course> {
    return this.http.get<Course>(`${this.API_URL}/admin/courses/${id}`);
  }

  createCourse(course: CourseDto): Observable<Course> {
    return this.http.post<Course>(`${this.API_URL}/admin/courses`, course);
  }

  updateCourse(id: number, course: CourseDto): Observable<Course> {
    return this.http.put<Course>(`${this.API_URL}/admin/courses/${id}`, course);
  }

  deleteCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/admin/courses/${id}`);
  }

  toggleCourseStatus(id: number): Observable<Course> {
    return this.http.put<Course>(`${this.API_URL}/admin/courses/${id}/toggle-status`, {});
  }

  addQuizToCourse(courseId: number, quiz: Quiz): Observable<Quiz> {
    return this.http.post<Quiz>(`${this.API_URL}/admin/courses/${courseId}/quizzes`, quiz);
  }

  updateQuiz(courseId: number, quizId: number, quiz: Quiz): Observable<Quiz> {
    return this.http.put<Quiz>(`${this.API_URL}/admin/courses/${courseId}/quizzes/${quizId}`, quiz);
  }

  deleteQuiz(courseId: number, quizId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/admin/courses/${courseId}/quizzes/${quizId}`);
  }
}

