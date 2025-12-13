class Message {
  final String text;
  final bool isUser;
  final DateTime timestamp;
  final bool isAudio; // Indique si le message est un audio
  final String? audioBase64; // Base64 de l'audio (pour les réponses de l'assistant)
  final String? audioFilePath; // Chemin du fichier audio (pour les audios de l'utilisateur)

  Message({
    required this.text,
    required this.isUser,
    required this.timestamp,
    this.isAudio = false,
    this.audioBase64,
    this.audioFilePath,
  });
}