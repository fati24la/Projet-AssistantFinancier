class DashboardData {
  final double totalIncome;
  final double totalExpenses;
  final double totalSavings;
  final double totalDebt;
  final double financialHealthScore;
  final List<MonthlyData> monthlyData;
  final List<CategoryExpense> categoryExpenses;
  final List<SavingsGoalData> activeGoals;
  final int totalPoints;
  final int level;
  final int unreadNotifications;

  DashboardData({
    required this.totalIncome,
    required this.totalExpenses,
    required this.totalSavings,
    required this.totalDebt,
    required this.financialHealthScore,
    required this.monthlyData,
    required this.categoryExpenses,
    required this.activeGoals,
    required this.totalPoints,
    required this.level,
    required this.unreadNotifications,
  });

  factory DashboardData.fromJson(Map<String, dynamic> json) {
    return DashboardData(
      totalIncome: (json['totalIncome'] ?? 0).toDouble(),
      totalExpenses: (json['totalExpenses'] ?? 0).toDouble(),
      totalSavings: (json['totalSavings'] ?? 0).toDouble(),
      totalDebt: (json['totalDebt'] ?? 0).toDouble(),
      financialHealthScore: (json['financialHealthScore'] ?? 0).toDouble(),
      monthlyData: (json['monthlyData'] as List<dynamic>?)
              ?.map((e) => MonthlyData.fromJson(e))
              .toList() ??
          [],
      categoryExpenses: (json['categoryExpenses'] as List<dynamic>?)
              ?.map((e) => CategoryExpense.fromJson(e))
              .toList() ??
          [],
      activeGoals: (json['activeGoals'] as List<dynamic>?)
              ?.map((e) => SavingsGoalData.fromJson(e))
              .toList() ??
          [],
      totalPoints: json['totalPoints'] ?? 0,
      level: json['level'] ?? 1,
      unreadNotifications: json['unreadNotifications'] ?? 0,
    );
  }
}

class MonthlyData {
  final String month;
  final double income;
  final double expenses;

  MonthlyData({
    required this.month,
    required this.income,
    required this.expenses,
  });

  factory MonthlyData.fromJson(Map<String, dynamic> json) {
    return MonthlyData(
      month: json['month'] ?? '',
      income: (json['income'] ?? 0).toDouble(),
      expenses: (json['expenses'] ?? 0).toDouble(),
    );
  }
}

class CategoryExpense {
  final String category;
  final double amount;
  final double percentage;

  CategoryExpense({
    required this.category,
    required this.amount,
    required this.percentage,
  });

  factory CategoryExpense.fromJson(Map<String, dynamic> json) {
    return CategoryExpense(
      category: json['category'] ?? '',
      amount: (json['amount'] ?? 0).toDouble(),
      percentage: (json['percentage'] ?? 0).toDouble(),
    );
  }
}

class SavingsGoalData {
  final int id;
  final String name;
  final String description;
  final double targetAmount;
  final double currentAmount;
  final double remainingAmount;
  final double progressPercentage;
  final String targetDate;
  final int daysRemaining;
  final bool completed;

  SavingsGoalData({
    required this.id,
    required this.name,
    required this.description,
    required this.targetAmount,
    required this.currentAmount,
    required this.remainingAmount,
    required this.progressPercentage,
    required this.targetDate,
    required this.daysRemaining,
    required this.completed,
  });

  factory SavingsGoalData.fromJson(Map<String, dynamic> json) {
    return SavingsGoalData(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      description: json['description'] ?? '',
      targetAmount: (json['targetAmount'] ?? 0).toDouble(),
      currentAmount: (json['currentAmount'] ?? 0).toDouble(),
      remainingAmount: (json['remainingAmount'] ?? 0).toDouble(),
      progressPercentage: (json['progressPercentage'] ?? 0).toDouble(),
      targetDate: json['targetDate'] ?? '',
      daysRemaining: json['daysRemaining'] ?? 0,
      completed: json['completed'] ?? false,
    );
  }
}

