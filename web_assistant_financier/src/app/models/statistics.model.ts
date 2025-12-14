export interface DashboardStatistics {
  totalUsers: number;
  totalBudgets: number;
  totalExpenses: number;
  totalCourses: number;
  completedCourses: number;
  userEvolution?: Array<{ date: string; count: number } | any>;
  budgetCategoryDistribution?: Array<{ category: string; amount: number } | any>;
  topUsersByPoints?: Array<{ username: string; points: number } | any>;
  recentActivities?: any[];
}

export interface UserEvolution {
  date: string;
  count: number;
}

export interface BudgetCategoryDistribution {
  category: string;
  count: number;
}

export interface TopUser {
  username: string;
  points: number;
}

export interface CourseStatistics {
  courseId: number;
  courseTitle: string;
  completions: number;
  averageScore: number;
}

export interface NotificationHistory {
  id: number;
  title: string;
  message: string;
  sentAt: string;
  recipientsCount: number;
}

