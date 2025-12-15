import 'dart:convert';
import 'package:http/http.dart' as http;
import '../auth_service.dart';
import '../models/notification_item.dart';
import '../storage_service.dart';

class NotificationService {
  static String _baseUrl() {
    return AuthService.baseUrl.replaceAll('/api/auth', '');
  }

  static Future<List<AppNotification>> fetchNotifications() async {
    final token = await StorageService.getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('${_baseUrl()}/api/notifications'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = json.decode(response.body);
      return data.map((e) => AppNotification.fromJson(e)).toList();
    }

    throw Exception('Erreur ${response.statusCode} lors du chargement');
  }

  static Future<void> markAsRead(int id) async {
    final token = await StorageService.getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.put(
      Uri.parse('${_baseUrl()}/api/notifications/$id/read'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode != 200) {
      throw Exception('Impossible de marquer comme lue (${response.statusCode})');
    }
  }

  static Future<void> markAllAsRead() async {
    final token = await StorageService.getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.put(
      Uri.parse('${_baseUrl()}/api/notifications/read-all'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode != 200) {
      throw Exception('Impossible de tout marquer comme lu (${response.statusCode})');
    }
  }

  static Future<void> deleteNotification(int id) async {
    final token = await StorageService.getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.delete(
      Uri.parse('${_baseUrl()}/api/notifications/$id'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode != 200) {
      throw Exception('Impossible de supprimer (${response.statusCode})');
    }
  }
}

