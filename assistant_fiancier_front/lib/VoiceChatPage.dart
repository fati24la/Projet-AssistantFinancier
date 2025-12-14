import 'dart:async';
import 'dart:io';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_sound/public/flutter_sound_recorder.dart';
import 'package:flutter_sound/flutter_sound.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';

import 'ChatApiService.dart';
import 'Message.dart';
import 'storage_service.dart';
import 'LoginPage.dart';
import 'auth_service.dart';

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
  late FlutterSoundPlayer _player;
  bool _isRecording = false;
  bool _isListening = false;
  bool _isTyping = false;
  bool _recorderInitialized = false;
  bool _playerInitialized = false;
  bool _isPlaying = false;
  String? _currentlyPlayingMessageId;

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

    // 🔹 Initialiser le recorder et le player
    _recorder = FlutterSoundRecorder();
    _player = FlutterSoundPlayer();

    // Charger les messages sauvegardés
    _loadMessages();
  }

  // Charger les messages depuis le stockage local
  Future<void> _loadMessages() async {
    try {
      final savedMessages = await StorageService.loadMessages();
      if (savedMessages.isNotEmpty) {
        setState(() {
          _messages.clear();
          for (var msgJson in savedMessages) {
            _messages.add(Message.fromJson(msgJson));
          }
        });
        // Faire défiler vers le bas après le chargement
        WidgetsBinding.instance.addPostFrameCallback((_) {
          _scrollToBottom();
        });
        print('✅ [VoiceChatPage] ${_messages.length} messages chargés');
      } else {
        // Si aucun message sauvegardé, ajouter le message d'accueil
        setState(() {
          _messages.add(Message(
            text: "Bonjour ! Je suis votre assistant financier. Comment puis-je vous aider aujourd'hui ?",
            isUser: false,
            timestamp: DateTime.now(),
          ));
        });
        await _saveMessages();
      }
    } catch (e) {
      print('❌ [VoiceChatPage] Erreur lors du chargement des messages: $e');
      // En cas d'erreur, ajouter le message d'accueil
      setState(() {
        _messages.add(Message(
          text: "Bonjour ! Je suis votre assistant financier. Comment puis-je vous aider aujourd'hui ?",
          isUser: false,
          timestamp: DateTime.now(),
        ));
      });
      await _saveMessages();
    }
  }

  // Sauvegarder les messages dans le stockage local
  Future<void> _saveMessages() async {
    try {
      final messagesJson = _messages.map((msg) => msg.toJson()).toList();
      await StorageService.saveMessages(messagesJson);
      print('✅ [VoiceChatPage] ${_messages.length} messages sauvegardés');
    } catch (e) {
      print('❌ [VoiceChatPage] Erreur lors de la sauvegarde des messages: $e');
    }
  }

  // Méthode de déconnexion
  Future<void> _logout() async {
    // Afficher une boîte de dialogue de confirmation
    final confirm = await showDialog<bool>(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Déconnexion'),
          content: const Text('Êtes-vous sûr de vouloir vous déconnecter ?'),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(false),
              child: const Text('Annuler'),
            ),
            TextButton(
              onPressed: () => Navigator.of(context).pop(true),
              child: const Text('Déconnexion'),
            ),
          ],
        );
      },
    );

    if (confirm == true) {
      // Effacer les données d'authentification et les messages
      await StorageService.clearAuth();
      await StorageService.clearMessages();
      
      // Rediriger vers la page de login
      if (mounted) {
        Navigator.pushAndRemoveUntil(
          context,
          MaterialPageRoute(builder: (context) => const LoginPage()),
          (route) => false, // Supprimer toutes les routes précédentes
        );
      }
    }
  }

  @override
  void dispose() async {
    _textController.dispose();
    _scrollController.dispose();
    _pulseController.dispose();
    _waveController.dispose();
    if (_recorderInitialized) {
      await _recorder.closeRecorder();
    }
    if (_playerInitialized) {
      await _player.closePlayer();
    }
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
    // Arrêter les animations
    _pulseController.stop();
    _waveController.stop();

    // Vérifier qu'un enregistrement était en cours
    if (!_isRecording || !_recorderInitialized) {
      if (mounted) {
        setState(() {
          _isRecording = false;
          _isListening = false;
        });
      }
      return;
    }

    try {
      File audioFile = await stopRecording();
      
      // Mettre à jour l'état après avoir arrêté l'enregistrement
      if (!mounted) return;
      
      setState(() {
        _isRecording = false;
        _isListening = false;
      });
      
      // Attendre un peu pour que l'UI se mette à jour
      await Future.delayed(const Duration(milliseconds: 100));
      
      if (!mounted) return;
      
      // Vérifier que le fichier existe et n'est pas vide
      if (!await audioFile.exists()) {
        throw Exception('Le fichier audio n\'existe pas');
      }

      // Vérifier la taille du fichier
      final fileSize = await audioFile.length();
      if (fileSize == 0) {
        throw Exception('Le fichier audio est vide (0 bytes). Veuillez réessayer.');
      }

      // Affiche un message audio de l'utilisateur avec le chemin du fichier
      if (!mounted) return;
      
      setState(() {
        _messages.add(Message(
          text: "🎤 Audio",
          isUser: true,
          timestamp: DateTime.now(),
          isAudio: true,
          audioFilePath: audioFile.path, // Sauvegarder le chemin pour pouvoir le réécouter
        ));
      });
      
      _saveMessages();
      _scrollToBottom();

      // Appel au backend (le userId est extrait du token côté backend)
      // Utiliser la même base URL que dans auth_service (sans /api/auth)
      final baseUrl = AuthService.baseUrl.replaceAll('/api/auth', '');
      print('🔗 [VoiceChatPage] Base URL: $baseUrl');
      print('📁 [VoiceChatPage] Fichier audio: ${audioFile.path} (${await audioFile.length()} bytes)');
      
      final response = await ChatApiService(baseUrl: baseUrl)
          .sendAudioQuestion(audioFile);
      
      print('✅ [VoiceChatPage] Réponse reçue du backend: $response');

      if (!mounted) return;

      // Extraire l'audio Base64 de la réponse
      final audioBase64 = response['audioBase64'] as String?;
      final answerText = response['answerText'] as String? ?? 'Réponse non disponible';

      setState(() {
        // Ajouter la réponse audio de l'assistant
        _messages.add(Message(
          text: "🎵 Réponse audio",
          isUser: false,
          timestamp: DateTime.now(),
          isAudio: true,
          audioBase64: audioBase64,
        ));
      });

      _saveMessages();
      if (mounted) {
        _scrollToBottom();
        // Jouer automatiquement l'audio de la réponse
        if (audioBase64 != null && audioBase64.isNotEmpty) {
          _playAudioFromBase64(audioBase64, _messages.length - 1);
        }
      }

    } catch (e, stackTrace) {
      // Mettre à jour l'état en cas d'erreur (uniquement si le widget est toujours monté)
      print('❌ [VoiceChatPage] Erreur lors de l\'envoi: $e');
      print('📚 [VoiceChatPage] Stack trace: $stackTrace');
      
      if (!mounted) return;
      
      // Extraire le message d'erreur
      String errorMessage = "Erreur lors de l'envoi de l'audio : $e";
      bool shouldLogout = false;
      
      if (e.toString().contains('403') || e.toString().contains('401')) {
        errorMessage = "Erreur d'authentification. Veuillez vous reconnecter.";
        shouldLogout = true;
      } else if (e.toString().contains('vide') || e.toString().contains('empty')) {
        errorMessage = "Le fichier audio est vide. Veuillez parler plus longtemps et réessayer.";
      } else if (e.toString().contains('Connection') || e.toString().contains('timeout') || e.toString().contains('Timeout')) {
        errorMessage = "Erreur de connexion ou timeout. Vérifiez votre connexion internet et réessayez.";
      }
      
      // Nettoyer l'authentification si nécessaire
      if (shouldLogout) {
        await StorageService.clearAuth();
      }
      
      // Utiliser WidgetsBinding.instance.addPostFrameCallback pour éviter les problèmes de contexte
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (!mounted) return;
        setState(() {
          _isRecording = false;
          _isListening = false;
          
          // Ne rien supprimer, garder l'audio de l'utilisateur

          _messages.add(Message(
            text: errorMessage,
            isUser: false,
            timestamp: DateTime.now(),
          ));
        });
        _saveMessages();
        
        if (mounted && shouldLogout) {
          Navigator.pushReplacement(
            context,
            MaterialPageRoute(builder: (context) => const LoginPage()),
          );
        }
      });
    }
  }


  void _handleTextMessage() async {
    final text = _textController.text.trim();
    if (text.isEmpty) return;

    // Ajouter le message de l'utilisateur
    setState(() {
      _messages.add(Message(
        text: text,
        isUser: true,
        timestamp: DateTime.now(),
        isAudio: false,
      ));
    });

    _saveMessages();
    _textController.clear();
    _scrollToBottom();

    try {
      // Envoyer la question texte au backend
      final baseUrl = AuthService.baseUrl.replaceAll('/api/auth', '');
      print('📝 [VoiceChatPage] Envoi de la question texte: $text');
      
      final response = await ChatApiService(baseUrl: baseUrl)
          .sendTextQuestion(text);
      
      print('✅ [VoiceChatPage] Réponse texte reçue du backend: $response');

      if (!mounted) return;

      // Extraire la réponse texte
      final answerText = response['answerText'] as String? ?? 'Réponse non disponible';

      setState(() {
        // Ajouter la réponse texte de l'assistant (pas audio)
        _messages.add(Message(
          text: answerText,
          isUser: false,
          timestamp: DateTime.now(),
          isAudio: false, // Réponse texte, pas audio
        ));
      });

      _saveMessages();
      if (mounted) {
        _scrollToBottom();
      }

    } catch (e, stackTrace) {
      print('❌ [VoiceChatPage] Erreur lors de l\'envoi du texte: $e');
      print('📚 [VoiceChatPage] Stack trace: $stackTrace');
      
      if (!mounted) return;
      
      // Extraire le message d'erreur
      String errorMessage = "Erreur lors de l'envoi du message : $e";
      bool shouldLogout = false;
      
      if (e.toString().contains('403') || e.toString().contains('401')) {
        errorMessage = "Erreur d'authentification. Veuillez vous reconnecter.";
        shouldLogout = true;
      } else if (e.toString().contains('Connection') || e.toString().contains('timeout') || e.toString().contains('Timeout')) {
        errorMessage = "Erreur de connexion ou timeout. Vérifiez votre connexion internet et réessayez.";
      }
      
      // Nettoyer l'authentification si nécessaire
      if (shouldLogout) {
        await StorageService.clearAuth();
      }
      
      // Utiliser WidgetsBinding.instance.addPostFrameCallback pour éviter les problèmes de contexte
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (!mounted) return;
        setState(() {
          _messages.add(Message(
            text: errorMessage,
            isUser: false,
            timestamp: DateTime.now(),
            isAudio: false,
          ));
        });
        _saveMessages();
        
        if (mounted && shouldLogout) {
          Navigator.pushReplacement(
            context,
            MaterialPageRoute(builder: (context) => const LoginPage()),
          );
        }
      });
    }
  }

  Future<bool> requestPermissions() async {
    try {
      // Demander la permission du microphone
      var statusMic = await Permission.microphone.status;
      if (!statusMic.isGranted) {
        statusMic = await Permission.microphone.request();
      }

      // Sur Android 13+, on n'a plus besoin de permission storage pour les fichiers temporaires
      // Mais on demande quand même pour la compatibilité
      if (await Permission.storage.isDenied) {
        await Permission.storage.request();
      }

      return statusMic.isGranted;
    } catch (e) {
      print("Erreur lors de la demande de permissions: $e");
      return false;
    }
  }

  void _startRecording() async {
    try {
      // Demander les permissions
      bool granted = await requestPermissions();
      if (!granted) {
        // Ne pas afficher de SnackBar pour éviter les problèmes de contexte
        print("Permission microphone non accordée !");
        return;
      }

      // Obtenir le répertoire temporaire
      final tempDir = await getTemporaryDirectory();
      final timestamp = DateTime.now().millisecondsSinceEpoch;
      final path = '${tempDir.path}/audio_question_$timestamp.aac';

      // Initialiser le recorder s'il n'est pas déjà initialisé
      if (!_recorderInitialized) {
        try {
          // S'assurer que le recorder est complètement fermé avant de l'ouvrir
          try {
            await _recorder.closeRecorder();
            await Future.delayed(const Duration(milliseconds: 100));
          } catch (_) {
            // Ignorer si déjà fermé
          }
          
          await _recorder.openRecorder();
          _recorderInitialized = true;
          print('✅ [VoiceChatPage] Recorder initialisé');
        } catch (e) {
          print('❌ [VoiceChatPage] Erreur lors de l\'initialisation du recorder: $e');
          // Essayer une fois de plus avec un délai plus long
          try {
            await Future.delayed(const Duration(milliseconds: 300));
            await _recorder.closeRecorder();
            await Future.delayed(const Duration(milliseconds: 200));
            await _recorder.openRecorder();
            _recorderInitialized = true;
            print('✅ [VoiceChatPage] Recorder initialisé après retry');
          } catch (e2) {
            print('❌ [VoiceChatPage] Erreur lors du retry d\'initialisation: $e2');
            rethrow;
          }
        }
      }

      // Vérifier si le recorder est déjà en train d'enregistrer et l'arrêter si nécessaire
      try {
        bool isCurrentlyRecording = await _recorder.isRecording;
        if (isCurrentlyRecording) {
          print('⚠️ [VoiceChatPage] Le recorder est déjà en train d\'enregistrer, arrêt du précédent...');
          try {
            await _recorder.stopRecorder();
            await Future.delayed(const Duration(milliseconds: 200)); // Attendre un peu plus
            print('✅ [VoiceChatPage] Enregistrement précédent arrêté');
          } catch (e) {
            print('⚠️ [VoiceChatPage] Erreur lors de l\'arrêt du recorder précédent: $e');
            // Continuer quand même
          }
        }
      } catch (e) {
        print('⚠️ [VoiceChatPage] Erreur lors de la vérification isRecording: $e');
        // Continuer quand même
      }

      // Démarrer l'enregistrement
      try {
        await _recorder.startRecorder(
          toFile: path,
          codec: Codec.aacADTS,
        );
        print('✅ [VoiceChatPage] Enregistrement démarré: $path');
      } catch (e) {
        print('❌ [VoiceChatPage] Erreur lors du démarrage de l\'enregistrement: $e');
        // Si le démarrage échoue, essayer de réinitialiser complètement le recorder
        try {
          print('🔄 [VoiceChatPage] Tentative de réinitialisation complète...');
          // Fermer complètement
          try {
            if (await _recorder.isRecording) {
              await _recorder.stopRecorder();
            }
          } catch (_) {
            // Ignorer
          }
          
          try {
            await _recorder.closeRecorder();
          } catch (_) {
            // Ignorer si déjà fermé
          }
          
          _recorderInitialized = false;
          await Future.delayed(const Duration(milliseconds: 300));
          
          // Rouvrir
          await _recorder.openRecorder();
          _recorderInitialized = true;
          
          await Future.delayed(const Duration(milliseconds: 100));
          
          // Réessayer de démarrer
          await _recorder.startRecorder(
            toFile: path,
            codec: Codec.aacADTS,
          );
          print('✅ [VoiceChatPage] Enregistrement démarré après réinitialisation complète');
        } catch (e2) {
          print('❌ [VoiceChatPage] Erreur lors de la réinitialisation complète: $e2');
          _recorderInitialized = false;
          rethrow;
        }
      }

      // Mettre à jour l'état
      if (mounted) {
        setState(() {
          _isRecording = true;
          _isListening = true;
        });
        _pulseController.repeat(reverse: true);
        _waveController.repeat();
      }
    } catch (e) {
      print("Erreur lors du démarrage de l'enregistrement: $e");
      if (mounted) {
        setState(() {
          _isRecording = false;
          _isListening = false;
        });
      }
    }
  }


  Future<File> stopRecording() async {
    try {
      // Vérifier que le recorder est initialisé
      if (!_recorderInitialized) {
        print('❌ [VoiceChatPage] Le recorder n\'est pas initialisé');
        throw Exception('Le recorder n\'est pas initialisé');
      }

      // Essayer d'arrêter l'enregistrement, même si isRecording retourne false
      // (car cette vérification peut être peu fiable)
      String? path;
      try {
        // Vérifier d'abord si le recorder est en train d'enregistrer
        bool isCurrentlyRecording = await _recorder.isRecording;
        print('📊 [VoiceChatPage] État du recorder (isRecording): $isCurrentlyRecording');
        
        if (isCurrentlyRecording) {
          path = await _recorder.stopRecorder();
        } else {
          // Si isRecording retourne false mais qu'on pense qu'on enregistre,
          // on essaie quand même d'arrêter (au cas où)
          print('⚠️ [VoiceChatPage] isRecording=false mais on essaie quand même d\'arrêter...');
          try {
            path = await _recorder.stopRecorder();
          } catch (e) {
            print('⚠️ [VoiceChatPage] Erreur lors de l\'arrêt (normal si pas d\'enregistrement): $e');
            throw Exception('Aucun enregistrement en cours');
          }
        }
      } catch (e) {
        print('❌ [VoiceChatPage] Erreur lors de stopRecorder: $e');
        rethrow;
      }
      
      // Vérifier que le path n'est pas null ou vide
      if (path == null || path.isEmpty) {
        throw Exception('Le chemin du fichier audio est vide');
      }
      
      print('✅ [VoiceChatPage] Enregistrement arrêté: $path');

      // Attendre un peu plus longtemps pour que le fichier soit complètement écrit
      await Future.delayed(const Duration(milliseconds: 300));

      // Créer l'objet File et vérifier qu'il existe
      File audioFile = File(path);
      
      // Attendre jusqu'à ce que le fichier existe (avec timeout)
      int attempts = 0;
      while (!await audioFile.exists() && attempts < 10) {
        await Future.delayed(const Duration(milliseconds: 100));
        attempts++;
      }
      
      if (!await audioFile.exists()) {
        throw Exception('Le fichier audio n\'existe pas au chemin: $path');
      }

      // Vérifier que le fichier n'est pas vide
      int fileSize = 0;
      attempts = 0;
      while (fileSize == 0 && attempts < 10) {
        fileSize = await audioFile.length();
        if (fileSize == 0) {
          await Future.delayed(const Duration(milliseconds: 100));
          attempts++;
        }
      }
      
      if (fileSize == 0) {
        throw Exception('Le fichier audio est vide. Assurez-vous d\'avoir parlé pendant l\'enregistrement.');
      }

      // Fermer le recorder proprement après l'enregistrement
      // On le rouvrira au prochain enregistrement
      try {
        // Attendre un peu avant de fermer pour s'assurer que tout est écrit
        await Future.delayed(const Duration(milliseconds: 100));
        await _recorder.closeRecorder();
        _recorderInitialized = false;
        print('✅ [VoiceChatPage] Recorder fermé proprement');
      } catch (e) {
        print('⚠️ [VoiceChatPage] Erreur lors de la fermeture du recorder: $e');
        // Réinitialiser le flag même en cas d'erreur
        _recorderInitialized = false;
      }

      return audioFile;
    } catch (e) {
      print('❌ [VoiceChatPage] Erreur dans stopRecording: $e');
      // En cas d'erreur, on essaie de nettoyer le recorder
      try {
        // Essayer d'arrêter si en cours
        try {
          if (await _recorder.isRecording) {
            await _recorder.stopRecorder();
          }
        } catch (_) {
          // Ignorer
        }
        // Fermer le recorder pour le réinitialiser
        if (_recorderInitialized) {
          await _recorder.closeRecorder();
          _recorderInitialized = false;
          print('✅ [VoiceChatPage] Recorder fermé après erreur');
        }
      } catch (_) {
        // Ignorer les erreurs de nettoyage
        _recorderInitialized = false; // Réinitialiser le flag quand même
      }
      rethrow;
    }
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
            const Expanded(
              child: Column(
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
            ),
          ],
        ),
        actions: [
          PopupMenuButton<String>(
            icon: const Icon(Icons.more_vert, color: Colors.white),
            onSelected: (value) {
              if (value == 'logout') {
                _logout();
              }
            },
            itemBuilder: (BuildContext context) => [
              const PopupMenuItem<String>(
                value: 'logout',
                child: Row(
                  children: [
                    Icon(Icons.logout, color: Colors.red),
                    SizedBox(width: 8),
                    Text('Déconnexion'),
                  ],
                ),
              ),
            ],
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
                // Bouton vocal avec support pour press-and-hold et tap
                GestureDetector(
                  onTapDown: (_) {
                    if (!_isRecording) {
                      _startRecording();
                    }
                  },
                  onTapUp: (_) {
                    if (_isRecording) {
                      _handleStopRecordingAndSend();
                    }
                  },
                  onTapCancel: () {
                    if (_isRecording) {
                      _handleStopRecordingAndSend();
                    }
                  },
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
    final messageId = message.timestamp.millisecondsSinceEpoch.toString();
    final isCurrentlyPlaying = _currentlyPlayingMessageId == messageId && _isPlaying;
    
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
                  child: message.isAudio && ((message.audioBase64 != null && !message.isUser) || (message.audioFilePath != null && message.isUser))
                      ? Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            IconButton(
                              icon: Icon(
                                isCurrentlyPlaying ? Icons.pause : Icons.play_arrow,
                                color: message.isUser ? Colors.white : const Color(0xFF4DD0E1),
                                size: 28,
                              ),
                              onPressed: () {
                                if (message.isUser && message.audioFilePath != null) {
                                  _playAudioFromFile(message.audioFilePath!, _messages.indexOf(message));
                                } else if (!message.isUser && message.audioBase64 != null) {
                                  _playAudioFromBase64(message.audioBase64!, _messages.indexOf(message));
                                }
                              },
                            ),
                            const SizedBox(width: 8),
                            Text(
                              isCurrentlyPlaying ? "Lecture en cours..." : (message.isUser ? "Audio envoyé" : "Réponse audio"),
                              style: TextStyle(
                                color: message.isUser ? Colors.white : Colors.black87,
                                fontSize: 15,
                              ),
                            ),
                          ],
                        )
                      : Text(
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

  Future<void> _playAudioFromBase64(String audioBase64, int messageIndex) async {
    try {
      final messageId = _messages[messageIndex].timestamp.millisecondsSinceEpoch.toString();
      
      // Si c'est le même message qui est en train de jouer, on arrête
      if (_isPlaying && _currentlyPlayingMessageId == messageId) {
        await _player.stopPlayer();
        if (mounted) {
          setState(() {
            _isPlaying = false;
            _currentlyPlayingMessageId = null;
          });
        }
        return;
      }
      
      // Décoder le Base64
      final audioBytes = base64Decode(audioBase64);
      
      // Sauvegarder dans un fichier temporaire
      final tempDir = await getTemporaryDirectory();
      final audioFile = File('${tempDir.path}/response_audio_${DateTime.now().millisecondsSinceEpoch}.mp3');
      await audioFile.writeAsBytes(audioBytes);
      
      print('🎵 [VoiceChatPage] Audio sauvegardé: ${audioFile.path} (${audioBytes.length} bytes)');
      
      // Initialiser le player si nécessaire
      if (!_playerInitialized) {
        await _player.openPlayer();
        _playerInitialized = true;
      }
      
      // Arrêter la lecture précédente si elle est en cours
      if (_isPlaying) {
        await _player.stopPlayer();
      }
      
      // Mettre à jour l'état pour afficher l'indicateur de lecture
      setState(() {
        _isPlaying = true;
        _currentlyPlayingMessageId = messageId;
      });
      
      // Jouer l'audio
      await _player.startPlayer(
        fromURI: audioFile.path,
        codec: Codec.mp3,
        whenFinished: () {
          if (mounted) {
            setState(() {
              _isPlaying = false;
              _currentlyPlayingMessageId = null;
            });
          }
          // Nettoyer le fichier temporaire après la lecture
          audioFile.delete();
        },
      );
      
    } catch (e) {
      print('❌ [VoiceChatPage] Erreur lors de la lecture audio: $e');
      if (mounted) {
        setState(() {
          _isPlaying = false;
          _currentlyPlayingMessageId = null;
        });
      }
    }
  }

  Future<void> _playAudioFromFile(String audioFilePath, int messageIndex) async {
    try {
      final messageId = _messages[messageIndex].timestamp.millisecondsSinceEpoch.toString();
      
      // Si c'est le même message qui est en train de jouer, on arrête
      if (_isPlaying && _currentlyPlayingMessageId == messageId) {
        await _player.stopPlayer();
        if (mounted) {
          setState(() {
            _isPlaying = false;
            _currentlyPlayingMessageId = null;
          });
        }
        return;
      }
      
      // Vérifier que le fichier existe
      final audioFile = File(audioFilePath);
      if (!await audioFile.exists()) {
        print('❌ [VoiceChatPage] Le fichier audio n\'existe pas: $audioFilePath');
        return;
      }
      
      print('🎵 [VoiceChatPage] Lecture de l\'audio: ${audioFile.path}');
      
      // Initialiser le player si nécessaire
      if (!_playerInitialized) {
        await _player.openPlayer();
        _playerInitialized = true;
      }
      
      // Arrêter la lecture précédente si elle est en cours
      if (_isPlaying) {
        await _player.stopPlayer();
      }
      
      // Mettre à jour l'état pour afficher l'indicateur de lecture
      setState(() {
        _isPlaying = true;
        _currentlyPlayingMessageId = messageId;
      });
      
      // Jouer l'audio (AAC depuis l'utilisateur)
      await _player.startPlayer(
        fromURI: audioFile.path,
        codec: Codec.aacADTS, // Utiliser AAC pour les enregistrements de l'utilisateur
        whenFinished: () {
          if (mounted) {
            setState(() {
              _isPlaying = false;
              _currentlyPlayingMessageId = null;
            });
          }
        },
      );
      
    } catch (e) {
      print('❌ [VoiceChatPage] Erreur lors de la lecture audio depuis fichier: $e');
      if (mounted) {
        setState(() {
          _isPlaying = false;
          _currentlyPlayingMessageId = null;
        });
      }
    }
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