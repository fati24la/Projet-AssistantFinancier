import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, UserDetails } from '../../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly API_URL = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getAllUsers(page: number = 0, size: number = 10, search?: string): Observable<{ content: User[]; totalElements: number }> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<{ content: User[]; totalElements: number }>(`${this.API_URL}/admin/users`, { params });
  }

  getUserById(id: number): Observable<UserDetails> {
    return this.http.get<UserDetails>(`${this.API_URL}/admin/users/${id}`);
  }

  toggleUserStatus(id: number): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/admin/users/${id}/toggle-status`, {});
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/admin/users/${id}`);
  }
}

