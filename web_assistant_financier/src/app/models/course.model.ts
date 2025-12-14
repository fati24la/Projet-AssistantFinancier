export interface Course {
  id: number;
  title: string;
  description: string;
  content: string;
  category: string; // BUDGETING, SAVINGS, CREDIT, INSURANCE, INCLUSION
  difficulty: string; // BEGINNER, INTERMEDIATE, ADVANCED
  durationMinutes: number;
  language: string; // FR, AR, AMZ
  createdAt: string;
  isActive: boolean;
  quizzes?: Quiz[];
}

export interface Quiz {
  id?: number;
  question: string;
  options: string[];
  correctAnswerIndex: number;
  explanation?: string;
  courseId?: number;
}

export interface CourseDto {
  title: string;
  description: string;
  content: string;
  category: string;
  difficulty: string;
  durationMinutes: number;
  language: string;
  isActive: boolean;
}

