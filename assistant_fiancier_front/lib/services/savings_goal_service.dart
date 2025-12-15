import 'dart:convert';

import 'package:http/http.dart' as http;

import '../auth_service.dart';
import '../models/savings_goal.dart';
import '../storage_service.dart';

class SavingsGoalService {
  static String get baseUrl {
    final authBaseUrl = AuthService.baseUrl.replaceAll('/api/auth', '');
    return authBaseUrl;
  }

  static Future<String?> _getToken() async {
    return StorageService.getToken();
  }

  static Future<List<SavingsGoal>> getGoals() async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/api/savings-goals'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = json.decode(response.body);
      return data.map((json) => SavingsGoal.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load savings goals: ${response.statusCode}');
    }
  }

  static Future<SavingsGoal> createGoal(SavingsGoal goal) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.post(
      Uri.parse('$baseUrl/api/savings-goals'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: json.encode(goal.toJson()),
    );

    if (response.statusCode == 200) {
      return SavingsGoal.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to create savings goal: ${response.statusCode}');
    }
  }

  static Future<SavingsGoal> addToGoal(int id, double amount) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.put(
      Uri.parse('$baseUrl/api/savings-goals/$id/add'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: json.encode(amount),
    );

    if (response.statusCode == 200) {
      return SavingsGoal.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to update savings goal: ${response.statusCode}');
    }
  }
}


