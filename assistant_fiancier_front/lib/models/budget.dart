class Budget {
  final int? id;
  final String name;
  final String category;
  final double amount;
  final double spent;
  final double remaining;
  final double? percentageUsed;
  final String startDate;
  final String endDate;
  final String period;

  Budget({
    this.id,
    required this.name,
    required this.category,
    required this.amount,
    required this.spent,
    required this.remaining,
    this.percentageUsed,
    required this.startDate,
    required this.endDate,
    required this.period,
  });

  factory Budget.fromJson(Map<String, dynamic> json) {
    return Budget(
      id: json['id'],
      name: json['name'] ?? '',
      category: json['category'] ?? '',
      amount: (json['amount'] ?? 0).toDouble(),
      spent: (json['spent'] ?? 0).toDouble(),
      remaining: (json['remaining'] ?? 0).toDouble(),
      percentageUsed: json['percentageUsed']?.toDouble(),
      startDate: json['startDate'] ?? '',
      endDate: json['endDate'] ?? '',
      period: json['period'] ?? 'MONTHLY',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'category': category,
      'amount': amount,
      'startDate': startDate,
      'endDate': endDate,
      'period': period,
    };
  }
}

