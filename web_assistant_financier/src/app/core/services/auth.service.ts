import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse } from '../../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api';
  private readonly TOKEN_KEY = 'admin_token';
  private readonly USERNAME_KEY = 'admin_username';
  private readonly USER_ID_KEY = 'admin_user_id';

  isAuthenticated = signal<boolean>(false);
  username = signal<string | null>(null);
  userId = signal<number | null>(null);

  constructor(private http: HttpClient) {
    this.loadAuthState();
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    console.log('🔐 [AuthService] Tentative de connexion:', credentials.username);
    return this.http.post<LoginResponse>(`${this.API_URL}/auth/login`, credentials).pipe(
      tap({
        next: (response) => {
          console.log('✅ [AuthService] Connexion réussie:', response.username);
          this.setAuthState(response);
        },
        error: (error) => {
          console.error('❌ [AuthService] Erreur de connexion:', error);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USERNAME_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
    this.isAuthenticated.set(false);
    this.username.set(null);
    this.userId.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private setAuthState(response: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USERNAME_KEY, response.username);
    localStorage.setItem(this.USER_ID_KEY, response.userId.toString());
    this.isAuthenticated.set(true);
    this.username.set(response.username);
    this.userId.set(response.userId);
  }

  private loadAuthState(): void {
    const token = this.getToken();
    if (token) {
      const username = localStorage.getItem(this.USERNAME_KEY);
      const userId = localStorage.getItem(this.USER_ID_KEY);
      this.isAuthenticated.set(true);
      this.username.set(username);
      this.userId.set(userId ? parseInt(userId, 10) : null);
    }
  }
}

