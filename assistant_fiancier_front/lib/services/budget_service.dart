import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/budget.dart';
import '../models/expense.dart';
import '../auth_service.dart';
import '../storage_service.dart';

class BudgetService {
  static String get baseUrl {
    final authBaseUrl = AuthService.baseUrl.replaceAll('/api/auth', '');
    return authBaseUrl;
  }

  static Future<String?> _getToken() async {
    return await StorageService.getToken();
  }

  // Budgets
  static Future<List<Budget>> getBudgets() async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/api/budgets'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = json.decode(response.body);
      return data.map((json) => Budget.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load budgets: ${response.statusCode}');
    }
  }

  static Future<Budget> createBudget(Budget budget) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.post(
      Uri.parse('$baseUrl/api/budgets'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: json.encode(budget.toJson()),
    );

    if (response.statusCode == 200) {
      return Budget.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to create budget: ${response.statusCode}');
    }
  }

  // Expenses
  static Future<List<Expense>> getExpenses() async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/api/expenses'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = json.decode(response.body);
      return data.map((json) => Expense.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load expenses: ${response.statusCode}');
    }
  }

  static Future<Expense> createExpense(Expense expense) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.post(
      Uri.parse('$baseUrl/api/expenses'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: json.encode(expense.toJson()),
    );

    if (response.statusCode == 200) {
      return Expense.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to create expense: ${response.statusCode}');
    }
  }
}

