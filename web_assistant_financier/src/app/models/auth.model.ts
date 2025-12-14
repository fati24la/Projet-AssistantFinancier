export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  userId: number;
}

export interface AuthState {
  isAuthenticated: boolean;
  token: string | null;
  username: string | null;
  userId: number | null;
}

