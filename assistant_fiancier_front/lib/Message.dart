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

  // Convertir Message en Map pour la sérialisation JSON
  Map<String, dynamic> toJson() {
    return {
      'text': text,
      'isUser': isUser,
      'timestamp': timestamp.toIso8601String(),
      'isAudio': isAudio,
      'audioBase64': audioBase64,
      'audioFilePath': audioFilePath,
    };
  }

  // Créer un Message à partir d'un Map (désérialisation JSON)
  factory Message.fromJson(Map<String, dynamic> json) {
    return Message(
      text: json['text'] as String,
      isUser: json['isUser'] as bool,
      timestamp: DateTime.parse(json['timestamp'] as String),
      isAudio: json['isAudio'] as bool? ?? false,
      audioBase64: json['audioBase64'] as String?,
      audioFilePath: json['audioFilePath'] as String?,
    );
  }
}