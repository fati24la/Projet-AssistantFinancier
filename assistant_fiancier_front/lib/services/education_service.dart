import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/course.dart';
import '../auth_service.dart';
import '../storage_service.dart';

class EducationService {
  static String get baseUrl {
    final authBaseUrl = AuthService.baseUrl.replaceAll('/api/auth', '');
    return authBaseUrl;
  }

  static Future<String?> _getToken() async {
    return await StorageService.getToken();
  }

  static Future<List<Course>> getCourses({String? language, String? category}) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    String url = '$baseUrl/api/education/courses';
    if (language != null || category != null) {
      final params = <String>[];
      if (language != null) params.add('language=$language');
      if (category != null) params.add('category=$category');
      url += '?${params.join('&')}';
    }

    final response = await http.get(
      Uri.parse(url),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = json.decode(response.body);
      return data.map((json) => Course.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load courses: ${response.statusCode}');
    }
  }

  static Future<Course> getCourse(int courseId) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.get(
      Uri.parse('$baseUrl/api/education/courses/$courseId'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return Course.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to load course: ${response.statusCode}');
    }
  }

  static Future<void> startCourse(int courseId) async {
    final token = await _getToken();
    if (token == null) {
      throw Exception('Not authenticated - Token is null');
    }

    print('🎓 [EducationService] Starting course $courseId');
    print('🔐 [EducationService] Token: ${token.substring(0, 20)}...');
    print('🔗 [EducationService] URL: $baseUrl/api/education/courses/$courseId/start');

    final response = await http.post(
      Uri.parse('$baseUrl/api/education/courses/$courseId/start'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    print('📡 [EducationService] Response status: ${response.statusCode}');
    print('📡 [EducationService] Response body: ${response.body}');

    if (response.statusCode == 401 || response.statusCode == 403) {
      throw Exception('Erreur d\'authentification. Veuillez vous reconnecter. (${response.statusCode})');
    } else if (response.statusCode != 200) {
      throw Exception('Erreur lors du démarrage du cours: ${response.statusCode} - ${response.body}');
    }
  }

  static Future<void> completeCourse(int courseId, int score) async {
    final token = await _getToken();
    if (token == null) throw Exception('Not authenticated');

    final response = await http.post(
      Uri.parse('$baseUrl/api/education/courses/$courseId/complete'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: json.encode({'score': score}),
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to complete course: ${response.statusCode}');
    }
  }
}

