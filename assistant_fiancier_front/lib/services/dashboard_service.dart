import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/dashboard_data.dart';
import '../auth_service.dart';
import '../storage_service.dart';

class DashboardService {
  static Future<DashboardData> getDashboard() async {
    final token = await StorageService.getToken();
    if (token == null) {
      throw Exception('Not authenticated');
    }

    final baseUrl = AuthService.baseUrl.replaceAll('/api/auth', '');
    final response = await http.get(
      Uri.parse('$baseUrl/api/dashboard'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return DashboardData.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to load dashboard: ${response.statusCode}');
    }
  }
}

