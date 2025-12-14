class Course {
  final int? id;
  final String title;
  final String description;
  final String content;
  final String category;
  final String difficulty;
  final int? durationMinutes;
  final String language;
  final bool isCompleted;
  final int? progress;
  final List<Quiz>? quizzes;

  Course({
    this.id,
    required this.title,
    required this.description,
    required this.content,
    required this.category,
    required this.difficulty,
    this.durationMinutes,
    required this.language,
    this.isCompleted = false,
    this.progress,
    this.quizzes,
  });

  factory Course.fromJson(Map<String, dynamic> json) {
    return Course(
      id: json['id'],
      title: json['title'] ?? '',
      description: json['description'] ?? '',
      content: json['content'] ?? '',
      category: json['category'] ?? '',
      difficulty: json['difficulty'] ?? 'BEGINNER',
      durationMinutes: json['durationMinutes'],
      language: json['language'] ?? 'FR',
      isCompleted: json['isCompleted'] ?? false,
      progress: json['progress'],
      quizzes: json['quizzes'] != null
          ? (json['quizzes'] as List).map((q) => Quiz.fromJson(q)).toList()
          : null,
    );
  }
}

class Quiz {
  final int? id;
  final String question;
  final List<String> options;
  final int? correctAnswerIndex;
  final String? explanation;

  Quiz({
    this.id,
    required this.question,
    required this.options,
    this.correctAnswerIndex,
    this.explanation,
  });

  factory Quiz.fromJson(Map<String, dynamic> json) {
    return Quiz(
      id: json['id'],
      question: json['question'] ?? '',
      options: json['options'] != null
          ? List<String>.from(json['options'])
          : [],
      correctAnswerIndex: json['correctAnswerIndex'],
      explanation: json['explanation'],
    );
  }
}

