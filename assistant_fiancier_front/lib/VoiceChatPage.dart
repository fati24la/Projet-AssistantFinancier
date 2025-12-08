import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_sound/public/flutter_sound_recorder.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';

import 'ChatApiService.dart';
import 'Message.dart';

class VoiceChatPage extends StatefulWidget {
  const VoiceChatPage({Key? key}) : super(key: key);

  @override
  State<VoiceChatPage> createState() => _VoiceChatPageState();
}

class _VoiceChatPageState extends State<VoiceChatPage> with TickerProviderStateMixin {
  final List<Message> _messages = [];
  final TextEditingController _textController = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  late FlutterSoundRecorder _recorder;
  bool _isRecording = false;
  bool _isListening = false;
  bool _isTyping = false;
  bool _recorderInitialized = false;

  late AnimationController _pulseController;
  late AnimationController _waveController;
  late Animation<double> _pulseAnimation;

  @override
  void initState() {
    super.initState();

    // Animation pour le bouton vocal
    _pulseController = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    );

    _pulseAnimation = Tween<double>(begin: 1.0, end: 1.2).animate(
      CurvedAnimation(parent: _pulseController, curve: Curves.easeInOut),
    );

    // Animation pour les ondes sonores
    _waveController = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    );

    // Message d'accueil
    _messages.add(Message(
      text: "Bonjour ! Je suis votre assistant financier. Comment puis-je vous aider aujourd'hui ?",
      isUser: false,
      timestamp: DateTime.now(),
    ));

    // 🔹 Initialiser le recorder
    _recorder = FlutterSoundRecorder();
  }

  @override
  void dispose() {
    _textController.dispose();
    _scrollController.dispose();
    _pulseController.dispose();
    _waveController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    if (_scrollController.hasClients) {
      Timer(const Duration(milliseconds: 300), () {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      });
    }
  }


  void _handleStopRecordingAndSend() async {
    setState(() {
      _isRecording = false;
      _isListening = false;
    });
    _pulseController.stop();
    _waveController.stop();

    try {
      File audioFile = await stopRecording();

      // Affiche le message "envoi audio"
      setState(() {
        _messages.add(Message(
          text: "…envoi de l'audio…",
          isUser: true,
          timestamp: DateTime.now(),
        ));
      });

      // Appel au backend
      String answer = await ChatApiService(baseUrl: "http://192.168.11.106:8080")
          .sendAudioQuestion(audioFile, 1);

      setState(() {
        _messages.add(Message(
          text: answer,
          isUser: false,
          timestamp: DateTime.now(),
        ));
      });

      _scrollToBottom();

    } catch (e) {
      setState(() {
        _messages.add(Message(
          text: "Erreur lors de l'envoi de l'audio : $e",
          isUser: false,
          timestamp: DateTime.now(),
        ));
      });
    }
  }


  void _handleTextMessage() {
    final text = _textController.text.trim();
    if (text.isEmpty) return;

    setState(() {
      _messages.add(Message(
        text: text,
        isUser: true,
        timestamp: DateTime.now(),
      ));
    });

    _textController.clear();
    _scrollToBottom();

  }

  Future<bool> requestPermissions() async {
    var statusMic = await Permission.microphone.request();
    var statusStorage = await Permission.storage.request();

    return statusMic.isGranted && statusStorage.isGranted;
  }

  void _startRecording() async {
    bool granted = await requestPermissions();
    if (!granted) {
      print("Permissions non accordées !");
      return;
    }

    final tempDir = await getTemporaryDirectory();
    final path = '${tempDir.path}/audio_question.aac';

    if (!_recorderInitialized) {
      await _recorder.openRecorder();
      _recorderInitialized = true;
    }

    await _recorder.startRecorder(toFile: path);

    setState(() {
      _isRecording = true;
      _isListening = true;
    });
    _pulseController.repeat(reverse: true);
    _waveController.repeat();
  }


  Future<File> stopRecording() async {
    if (!_isRecording) return File(''); // Aucun enregistrement en cours

    final path = await _recorder.stopRecorder();
    await _recorder.closeRecorder();
    _recorderInitialized = false;

    return File(path!);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        elevation: 0,
        backgroundColor: const Color(0xFF4DD0E1),
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: const BoxDecoration(
                color: Colors.white,
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.account_balance_wallet,
                color: Color(0xFF4DD0E1),
                size: 24,
              ),
            ),
            const SizedBox(width: 12),
            const Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Assistant Financier',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
                Text(
                  'En ligne',
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.white70,
                  ),
                ),
              ],
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.more_vert, color: Colors.white),
            onPressed: () {},
          ),
        ],
      ),
      body: Column(
        children: [
          // Zone de messages
          Expanded(
            child: ListView.builder(
              controller: _scrollController,
              padding: const EdgeInsets.all(16),
              itemCount: _messages.length + (_isTyping ? 1 : 0),
              itemBuilder: (context, index) {
                if (index == _messages.length && _isTyping) {
                  return _buildTypingIndicator();
                }

                final message = _messages[index];
                return _buildMessageBubble(message);
              },
            ),
          ),

          // Indicateur d'écoute
          if (_isListening)
            Container(
              padding: const EdgeInsets.symmetric(vertical: 12),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _buildWaveBar(0),
                  const SizedBox(width: 4),
                  _buildWaveBar(1),
                  const SizedBox(width: 4),
                  _buildWaveBar(2),
                  const SizedBox(width: 4),
                  _buildWaveBar(3),
                  const SizedBox(width: 4),
                  _buildWaveBar(4),
                  const SizedBox(width: 12),
                  const Text(
                    'Écoute en cours...',
                    style: TextStyle(
                      color: Color(0xFF4DD0E1),
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
            ),

          // Zone de saisie
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.white,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.05),
                  blurRadius: 10,
                  offset: const Offset(0, -5),
                ),
              ],
            ),
            child: Row(
              children: [
                // Bouton vocal
                GestureDetector(
                  onTapDown: (_) => _startRecording(),
                  onTapUp: (_) => _handleStopRecordingAndSend(),
                  onTapCancel: () => _handleStopRecordingAndSend(),
                  child: AnimatedBuilder(
                    animation: _pulseAnimation,
                    builder: (context, child) {
                      return Transform.scale(
                        scale: _isRecording ? _pulseAnimation.value : 1.0,
                        child: Container(
                          width: 56,
                          height: 56,
                          decoration: BoxDecoration(
                            color: _isRecording
                                ? Colors.red
                                : const Color(0xFF4DD0E1),
                            shape: BoxShape.circle,
                            boxShadow: [
                              BoxShadow(
                                color: (_isRecording ? Colors.red : const Color(0xFF4DD0E1))
                                    .withOpacity(0.3),
                                blurRadius: _isRecording ? 20 : 10,
                                spreadRadius: _isRecording ? 5 : 2,
                              ),
                            ],
                          ),
                          child: Icon(
                            _isRecording ? Icons.mic : Icons.mic_none,
                            color: Colors.white,
                            size: 28,
                          ),
                        ),
                      );
                    },
                  ),
                ),
                const SizedBox(width: 12),

                // Champ de texte
                Expanded(
                  child: Container(
                    decoration: BoxDecoration(
                      color: Colors.grey[100],
                      borderRadius: BorderRadius.circular(24),
                    ),
                    child: TextField(
                      controller: _textController,
                      decoration: InputDecoration(
                        hintText: 'Tapez votre message...',
                        border: InputBorder.none,
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 20,
                          vertical: 12,
                        ),
                        suffixIcon: IconButton(
                          icon: const Icon(Icons.attach_file),
                          onPressed: () {},
                          color: Colors.grey[600],
                        ),
                      ),
                      maxLines: null,
                      textInputAction: TextInputAction.send,
                      onSubmitted: (_) => _handleTextMessage(),
                    ),
                  ),
                ),
                const SizedBox(width: 8),

                // Bouton d'envoi
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: const Color(0xFF4DD0E1),
                    shape: BoxShape.circle,
                  ),
                  child: IconButton(
                    icon: const Icon(Icons.send, color: Colors.white),
                    onPressed: _handleTextMessage,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMessageBubble(Message message) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        mainAxisAlignment: message.isUser
            ? MainAxisAlignment.end
            : MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (!message.isUser) ...[
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: const Color(0xFF4DD0E1),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.smart_toy,
                color: Colors.white,
                size: 20,
              ),
            ),
            const SizedBox(width: 8),
          ],
          Flexible(
            child: Column(
              crossAxisAlignment: message.isUser
                  ? CrossAxisAlignment.end
                  : CrossAxisAlignment.start,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 12,
                  ),
                  decoration: BoxDecoration(
                    color: message.isUser
                        ? const Color(0xFF4DD0E1)
                        : Colors.white,
                    borderRadius: BorderRadius.only(
                      topLeft: const Radius.circular(20),
                      topRight: const Radius.circular(20),
                      bottomLeft: Radius.circular(message.isUser ? 20 : 4),
                      bottomRight: Radius.circular(message.isUser ? 4 : 20),
                    ),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.05),
                        blurRadius: 5,
                        offset: const Offset(0, 2),
                      ),
                    ],
                  ),
                  child: Text(
                    message.text,
                    style: TextStyle(
                      color: message.isUser ? Colors.white : Colors.black87,
                      fontSize: 15,
                    ),
                  ),
                ),
                const SizedBox(height: 4),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 8),
                  child: Text(
                    '${message.timestamp.hour}:${message.timestamp.minute.toString().padLeft(2, '0')}',
                    style: TextStyle(
                      fontSize: 11,
                      color: Colors.grey[500],
                    ),
                  ),
                ),
              ],
            ),
          ),
          if (message.isUser) ...[
            const SizedBox(width: 8),
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: Colors.grey[300],
                shape: BoxShape.circle,
              ),
              child: Icon(
                Icons.person,
                color: Colors.grey[600],
                size: 20,
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildTypingIndicator() {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          Container(
            width: 36,
            height: 36,
            decoration: BoxDecoration(
              color: const Color(0xFF4DD0E1),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.smart_toy,
              color: Colors.white,
              size: 20,
            ),
          ),
          const SizedBox(width: 8),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(20),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.05),
                  blurRadius: 5,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                _buildDot(0),
                const SizedBox(width: 4),
                _buildDot(1),
                const SizedBox(width: 4),
                _buildDot(2),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDot(int index) {
    return TweenAnimationBuilder<double>(
      tween: Tween(begin: 0.0, end: 1.0),
      duration: const Duration(milliseconds: 600),
      builder: (context, value, child) {
        final delay = index * 0.2;
        final animValue = ((value + delay) % 1.0);
        final opacity = (animValue < 0.5)
            ? animValue * 2
            : (1 - animValue) * 2;

        return Opacity(
          opacity: opacity,
          child: Container(
            width: 8,
            height: 8,
            decoration: BoxDecoration(
              color: Colors.grey[400],
              shape: BoxShape.circle,
            ),
          ),
        );
      },
      onEnd: () {
        if (_isTyping && mounted) {
          setState(() {});
        }
      },
    );
  }

  Widget _buildWaveBar(int index) {
    return AnimatedBuilder(
      animation: _waveController,
      builder: (context, child) {
        final delay = index * 0.1;
        final animValue = (_waveController.value + delay) % 1.0;
        final height = 4 + (20 * (0.5 - (animValue - 0.5).abs()) * 2);

        return Container(
          width: 4,
          height: height,
          decoration: BoxDecoration(
            color: const Color(0xFF4DD0E1),
            borderRadius: BorderRadius.circular(2),
          ),
        );
      },
    );
  }
}