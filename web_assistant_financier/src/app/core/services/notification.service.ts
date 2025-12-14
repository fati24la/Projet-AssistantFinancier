import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NotificationHistory } from '../../models/statistics.model';

export interface NotificationRequest {
  title: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly API_URL = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  broadcastNotification(notification: NotificationRequest): Observable<{ message: string; recipientsCount: number }> {
    return this.http.post<{ message: string; recipientsCount: number }>(
      `${this.API_URL}/admin/notifications/broadcast`,
      notification
    );
  }

  getNotificationHistory(): Observable<NotificationHistory[]> {
    return this.http.get<NotificationHistory[]>(`${this.API_URL}/admin/notifications/history`);
  }
}

