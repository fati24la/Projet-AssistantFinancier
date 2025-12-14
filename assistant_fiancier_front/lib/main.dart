import 'package:flutter/material.dart';

import 'LoginPage.dart';
import 'VoiceChatPage.dart';
import 'storage_service.dart';

void main() {
  runApp(const AssistantFinancierApp());
}

class AssistantFinancierApp extends StatelessWidget {
  const AssistantFinancierApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Assistant Financier',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primaryColor: const Color(0xFF4DD0E1),
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF4DD0E1),
          primary: const Color(0xFF4DD0E1),
        ),
        useMaterial3: true,
      ),
      home: const AuthWrapper(),
    );
  }
}

class AuthWrapper extends StatefulWidget {
  const AuthWrapper({Key? key}) : super(key: key);

  @override
  State<AuthWrapper> createState() => _AuthWrapperState();
}

class _AuthWrapperState extends State<AuthWrapper> {
  bool _isLoading = true;
  bool _isLoggedIn = false;

  @override
  void initState() {
    super.initState();
    _checkAuth();
  }

  Future<void> _checkAuth() async {
    final isLoggedIn = await StorageService.isLoggedIn();
    setState(() {
      _isLoggedIn = isLoggedIn;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }

    return _isLoggedIn ? const VoiceChatPage() : const LoginPage();
  }
}