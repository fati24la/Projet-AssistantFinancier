import { Course } from './course.model';

export interface User {
  id: number;
  username: string;
  email: string;
  createdAt?: string;
  enabled?: boolean;
}

export interface UserDetails extends User {
  budgets?: Budget[];
  expenses?: Expense[];
  progress?: UserProgress[];
  savingsGoals?: SavingsGoal[];
  totalPoints?: number;
}

export interface Budget {
  id: number;
  name: string;
  category: string;
  amount: number;
  spent: number;
  startDate: string;
  endDate: string;
  period: string;
  createdAt: string;
}

export interface Expense {
  id: number;
  description: string;
  category: string;
  amount: number;
  date: string;
  paymentMethod: string;
  createdAt: string;
}

export interface UserProgress {
  id: number;
  completed: boolean;
  score?: number;
  startedAt: string;
  completedAt?: string;
  course: Course;
}

export interface SavingsGoal {
  id: number;
  name: string;
  targetAmount: number;
  currentAmount: number;
  targetDate: string;
  createdAt: string;
}

