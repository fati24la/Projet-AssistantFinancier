import 'dart:convert';

import 'package:http/http.dart' as http;

import '../auth_service.dart';
import '../storage_service.dart';

class ProfileService {
  static String get baseUrl {
    final authBaseUrl = AuthService.baseUrl.replaceAll('/api/auth', '');
    return authBaseUrl;
  }

  static Future<String?> _getToken() async {
    return StorageService.getToken();
  }

  static Future<void> updateFinancialProfile({
    required double monthlyIncome,
    required double monthlyExpenses,
    required double totalSavings,
    required double totalDebt,
  }) async {
    final token = await _getToken();
    if (token == null) {
      throw Exception('Not authenticated');
    }

    final body = json.encode({
      'monthlyIncome': monthlyIncome,
      'monthlyExpenses': monthlyExpenses,
      'totalSavings': totalSavings,
      'totalDebt': totalDebt,
    });

    final response = await http.put(
      Uri.parse('$baseUrl/api/profile/financial'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: body,
    );

    if (response.statusCode != 200) {
      throw Exception(
          'Failed to update financial profile: ${response.statusCode}');
    }
  }
}


