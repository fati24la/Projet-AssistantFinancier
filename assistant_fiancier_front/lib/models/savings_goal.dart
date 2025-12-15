class SavingsGoal {
  final int? id;
  final String name;
  final String description;
  final double targetAmount;
  final double currentAmount;
  final double remainingAmount;
  final double progressPercentage;
  final String targetDate;
  final int daysRemaining;
  final bool completed;

  SavingsGoal({
    this.id,
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

  factory SavingsGoal.fromJson(Map<String, dynamic> json) {
    return SavingsGoal(
      id: json['id'],
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

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'description': description,
      'targetAmount': targetAmount,
      'targetDate': targetDate,
    };
  }
}


