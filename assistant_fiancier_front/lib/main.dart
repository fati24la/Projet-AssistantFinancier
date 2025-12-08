import 'package:flutter/material.dart';

import 'LoginPage.dart';

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
      home: const LoginPage(),
    );
  }
}