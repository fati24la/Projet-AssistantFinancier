import 'dart:io';
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:path/path.dart';
import 'storage_service.dart';

class ChatApiService {
  final String baseUrl;

  ChatApiService({required this.baseUrl});

  Future<Map<String, dynamic>> sendAudioQuestion(File audioFile) async {
    print('📤 [ChatApiService] Début de l\'envoi de l\'audio...');
    
    // Vérifier que le fichier existe et n'est pas vide
    if (!await audioFile.exists()) {
      print('❌ [ChatApiService] Le fichier audio n\'existe pas');
      throw Exception('Le fichier audio n\'existe pas');
    }
    
    final fileSize = await audioFile.length();
    print('📊 [ChatApiService] Taille du fichier: $fileSize bytes');
    if (fileSize == 0) {
      print('❌ [ChatApiService] Le fichier audio est vide');
      throw Exception('Le fichier audio est vide (0 bytes). Veuillez parler plus longtemps.');
    }

    // Récupérer le token
    final token = await StorageService.getToken();
    if (token == null || token.isEmpty) {
      print('❌ [ChatApiService] Token manquant');
      throw Exception('Token d\'authentification manquant. Veuillez vous reconnecter.');
    }
    print('✅ [ChatApiService] Token récupéré (longueur: ${token.length})');

    var uri = Uri.parse('$baseUrl/chat/audio-question');
    print('🌐 [ChatApiService] URL: $uri');
    var request = http.MultipartRequest('POST', uri);

    // Ajouter le header Authorization
    request.headers['Authorization'] = 'Bearer $token';

    // Le userId n'est plus nécessaire car il est extrait du token côté backend
    print('📎 [ChatApiService] Ajout du fichier: ${audioFile.path}');
    request.files.add(await http.MultipartFile.fromPath(
      'file',
      audioFile.path,
      filename: basename(audioFile.path),
    ));

    print('🚀 [ChatApiService] Envoi de la requête...');
    var response = await request.send().timeout(
      const Duration(seconds: 180), // 3 minutes pour Whisper + Gemini + TTS
      onTimeout: () {
        print('⏱️ [ChatApiService] Timeout après 180 secondes');
        throw Exception('Timeout: La requête a pris trop de temps. Le traitement audio peut prendre du temps.');
      },
    );

    print('📥 [ChatApiService] Réponse reçue: ${response.statusCode}');

    if (response.statusCode == 200) {
      final respStr = await response.stream.bytesToString();
      print('✅ [ChatApiService] Réponse reçue (${respStr.length} caractères)');
      // Parser le JSON ChatAnswerDto
      try {
        final Map<String, dynamic> jsonResponse = jsonDecode(respStr);
        print('✅ [ChatApiService] JSON parsé avec succès');
        return jsonResponse;
      } catch (e) {
        print('❌ [ChatApiService] Erreur de parsing JSON: $e');
        print('📄 [ChatApiService] Contenu de la réponse: $respStr');
        throw Exception('Erreur lors du parsing de la réponse: $e');
      }
    } else if (response.statusCode == 401 || response.statusCode == 403) {
      // Token invalide, expiré ou non autorisé
      final errorBody = await response.stream.bytesToString();
      print('❌ [ChatApiService] Erreur d\'authentification (${response.statusCode}): $errorBody');
      await StorageService.clearAuth();
      throw Exception('Session expirée ou non autorisée (${response.statusCode}). Veuillez vous reconnecter.');
    } else {
      final errorBody = await response.stream.bytesToString();
      print('❌ [ChatApiService] Erreur API (${response.statusCode}): $errorBody');
      throw Exception('Erreur API: ${response.statusCode} - $errorBody');
    }
  }
}
