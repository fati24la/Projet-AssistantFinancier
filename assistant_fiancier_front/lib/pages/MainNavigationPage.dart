import 'package:flutter/material.dart';
import 'DashboardPage.dart';
import 'BudgetPage.dart';
import 'EducationPage.dart';
import 'NotificationsPage.dart';
import 'ProfilePage.dart';
import '../VoiceChatPage.dart';

class MainNavigationPage extends StatefulWidget {
  const MainNavigationPage({Key? key}) : super(key: key);

  @override
  State<MainNavigationPage> createState() => _MainNavigationPageState();
}

class _MainNavigationPageState extends State<MainNavigationPage> {
  int _currentIndex = 0;
  final GlobalKey _dashboardKey = GlobalKey();

  late final List<Widget> _pages = [
    DashboardPage(key: _dashboardKey),
    const BudgetPage(),
    const EducationPage(),
    const NotificationsPage(),
    const VoiceChatPage(),
    const ProfilePage(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _currentIndex,
        children: _pages,
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        selectedItemColor: const Color(0xFF4DD0E1),
        unselectedItemColor: Colors.grey,
        type: BottomNavigationBarType.fixed,
        onTap: (index) {
          setState(() {
            _currentIndex = index;
          });
          if (index == 0) {
            final state = _dashboardKey.currentState;
            if (state != null) {
              // Appel dynamique de la méthode reload() exposée par DashboardPage
              (state as dynamic).reload();
            }
          }
        },
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.dashboard),
            label: 'Tableau de bord',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.account_balance_wallet),
            label: 'Budget',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.school),
            label: 'Guides',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.notifications),
            label: 'Notifications',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.chat),
            label: 'Assistant',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.person),
            label: 'Profil',
          ),
        ],
      ),
    );
  }
}

