import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:path/path.dart';

class ChatApiService {
  final String baseUrl;

  ChatApiService({required this.baseUrl});

  Future<String> sendAudioQuestion(File audioFile, int userId) async {
    var uri = Uri.parse('$baseUrl/chat/audio-question');
    var request = http.MultipartRequest('POST', uri);

    request.fields['userId'] = userId.toString();
    request.files.add(await http.MultipartFile.fromPath(
      'file',
      audioFile.path,
      filename: basename(audioFile.path),
    ));

    var response = await request.send();

    if (response.statusCode == 200) {
      final respStr = await response.stream.bytesToString();
      return respStr; // texte de l'assistant
    } else {
      throw Exception('Erreur API: ${response.statusCode}');
    }
  }
}
