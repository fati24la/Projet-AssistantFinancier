class Expense {
  final int? id;
  final String description;
  final String category;
  final double amount;
  final String date;
  final String paymentMethod;

  Expense({
    this.id,
    required this.description,
    required this.category,
    required this.amount,
    required this.date,
    required this.paymentMethod,
  });

  factory Expense.fromJson(Map<String, dynamic> json) {
    return Expense(
      id: json['id'],
      description: json['description'] ?? '',
      category: json['category'] ?? '',
      amount: (json['amount'] ?? 0).toDouble(),
      date: json['date'] ?? '',
      paymentMethod: json['paymentMethod'] ?? 'CARD',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'description': description,
      'category': category,
      'amount': amount,
      'date': date,
      'paymentMethod': paymentMethod,
    };
  }
}

