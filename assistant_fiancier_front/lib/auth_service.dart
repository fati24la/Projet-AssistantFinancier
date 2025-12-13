import 'dart:convert';
import 'package:http/http.dart' as http;

class AuthService {
  static const String baseUrl = "http://192.168.11.206:8080/api/auth"; // Utilisation de la même IP partout

  // ---------------- REGISTER ----------------
  static Future<Map<String, dynamic>> register({
    required String username,
    required String email,
    required String password,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/register'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'username': username,
          'email': email,
          'password': password,
        }),
      ).timeout(const Duration(seconds: 10));

      // Essayer de parser le body comme JSON, sinon retourner la string
      dynamic body;
      try {
        body = jsonDecode(response.body);
      } catch (e) {
        body = response.body;
      }

      return {
        'statusCode': response.statusCode,
        'body': body,
      };
    } catch (e) {
      // Gérer les erreurs réseau
      return {
        'statusCode': 0,
        'body': 'Erreur de connexion: ${e.toString()}',
      };
    }
  }

  // ---------------- LOGIN ----------------
  static Future<Map<String, dynamic>> login({
    required String username,
    required String password,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'username': username,
          'password': password,
        }),
      ).timeout(const Duration(seconds: 10));

      // Essayer de parser le body comme JSON
      dynamic body;
      try {
        body = jsonDecode(response.body);
      } catch (e) {
        body = response.body;
      }

      return {
        'statusCode': response.statusCode,
        'body': body,
      };
    } catch (e) {
      // Gérer les erreurs réseau
      return {
        'statusCode': 0,
        'body': 'Erreur de connexion: ${e.toString()}',
      };
    }
  }
}
