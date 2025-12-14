import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  DashboardStatistics,
  UserEvolution,
  BudgetCategoryDistribution,
  TopUser,
  CourseStatistics
} from '../../models/statistics.model';

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {
  private readonly API_URL = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getDashboardStatistics(): Observable<DashboardStatistics> {
    return this.http.get<DashboardStatistics>(`${this.API_URL}/admin/statistics/dashboard`);
  }

  getUserEvolution(): Observable<UserEvolution[]> {
    return this.http.get<UserEvolution[]>(`${this.API_URL}/admin/statistics/user-evolution`);
  }

  getBudgetCategoryDistribution(): Observable<BudgetCategoryDistribution[]> {
    return this.http.get<BudgetCategoryDistribution[]>(`${this.API_URL}/admin/statistics/budget-categories`);
  }

  getTopUsers(limit: number = 5): Observable<TopUser[]> {
    return this.http.get<TopUser[]>(`${this.API_URL}/admin/statistics/top-users?limit=${limit}`);
  }

  getCourseStatistics(): Observable<CourseStatistics[]> {
    return this.http.get<CourseStatistics[]>(`${this.API_URL}/admin/statistics/courses`);
  }

  exportData(format: 'csv' | 'pdf'): Observable<Blob> {
    return this.http.get(`${this.API_URL}/admin/statistics/export?format=${format}`, {
      responseType: 'blob'
    });
  }
}

