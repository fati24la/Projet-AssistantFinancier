import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class StorageService {
  static const String _keyToken = 'auth_token';
  static const String _keyUserId = 'user_id';
  static const String _keyUsername = 'username';

  // Sauvegarder le token
  static Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyToken, token);
  }

  // Récupérer le token
  static Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyToken);
  }

  // Sauvegarder le userId
  static Future<void> saveUserId(int userId) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_keyUserId, userId);
  }

  // Récupérer le userId
  static Future<int?> getUserId() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getInt(_keyUserId);
  }

  // Sauvegarder le username
  static Future<void> saveUsername(String username) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyUsername, username);
  }

  // Récupérer le username
  static Future<String?> getUsername() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyUsername);
  }

  // Supprimer toutes les données d'authentification
  static Future<void> clearAuth() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_keyToken);
    await prefs.remove(_keyUserId);
    await prefs.remove(_keyUsername);
  }

  // Vérifier si l'utilisateur est connecté
  static Future<bool> isLoggedIn() async {
    final token = await getToken();
    return token != null && token.isNotEmpty;
  }

  // Sauvegarder les messages de conversation pour un utilisateur spécifique
  static Future<void> saveMessages(List<Map<String, dynamic>> messages, int userId) async {
    final prefs = await SharedPreferences.getInstance();
    // Convertir la liste en JSON string avec une clé spécifique à l'utilisateur
    final messagesJson = jsonEncode(messages);
    final key = 'conversation_messages_$userId';
    await prefs.setString(key, messagesJson);
    print('✅ [StorageService] Messages sauvegardés pour userId: $userId');
  }

  // Charger les messages de conversation pour un utilisateur spécifique
  static Future<List<Map<String, dynamic>>> loadMessages(int userId) async {
    final prefs = await SharedPreferences.getInstance();
    final key = 'conversation_messages_$userId';
    final messagesJson = prefs.getString(key);
    if (messagesJson == null || messagesJson.isEmpty) {
      print('📭 [StorageService] Aucun message trouvé pour userId: $userId');
      return [];
    }
    try {
      final List<dynamic> decoded = jsonDecode(messagesJson);
      print('✅ [StorageService] ${decoded.length} messages chargés pour userId: $userId');
      return decoded.cast<Map<String, dynamic>>();
    } catch (e) {
      print('❌ [StorageService] Erreur lors du chargement des messages: $e');
      return [];
    }
  }

  // Effacer les messages de conversation pour un utilisateur spécifique
  static Future<void> clearMessages(int userId) async {
    final prefs = await SharedPreferences.getInstance();
    final key = 'conversation_messages_$userId';
    await prefs.remove(key);
    print('🗑️ [StorageService] Messages effacés pour userId: $userId');
  }
}

