import 'package:flutter/material.dart';
import '../models/dashboard_data.dart';
import '../services/dashboard_service.dart';
import '../services/profile_service.dart';
import '../storage_service.dart';
import '../LoginPage.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({Key? key}) : super(key: key);

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  DashboardData? _dashboardData;
  String? _username;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadProfileData();
  }

  Future<void> _loadProfileData() async {
    try {
      // Charger les données du dashboard (contient les infos du profil)
      final data = await DashboardService.getDashboard();
      final username = await StorageService.getUsername();
      
      setState(() {
        _dashboardData = data;
        _username = username;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erreur lors du chargement: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  Future<void> _showEditFinancialDialog() async {
    if (_dashboardData == null) return;

    // Approximation : totalIncome représente 6 mois => revenu mensuel estimé
    final estimatedMonthlyIncome = _dashboardData!.totalIncome / 6;

    final incomeController =
        TextEditingController(text: estimatedMonthlyIncome.toStringAsFixed(2));
    final expensesController = TextEditingController(
        text: _dashboardData!.totalExpenses.toStringAsFixed(2));
    final savingsController = TextEditingController(
        text: _dashboardData!.totalSavings.toStringAsFixed(2));
    final debtController = TextEditingController(
        text: _dashboardData!.totalDebt.toStringAsFixed(2));

    final result = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Mettre à jour mon profil financier'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: incomeController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Revenu mensuel (MAD)',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: expensesController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Dépenses mensuelles (MAD)',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: savingsController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Épargne totale (MAD)',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: debtController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Dette totale (MAD)',
                  border: OutlineInputBorder(),
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Annuler'),
          ),
          ElevatedButton(
            onPressed: () async {
              try {
                final monthlyIncome =
                    double.tryParse(incomeController.text) ?? 0;
                final monthlyExpenses =
                    double.tryParse(expensesController.text) ?? 0;
                final totalSavings =
                    double.tryParse(savingsController.text) ?? 0;
                final totalDebt = double.tryParse(debtController.text) ?? 0;

                await ProfileService.updateFinancialProfile(
                  monthlyIncome: monthlyIncome,
                  monthlyExpenses: monthlyExpenses,
                  totalSavings: totalSavings,
                  totalDebt: totalDebt,
                );

                if (mounted) {
                  Navigator.pop(context, true);
                }
              } catch (e) {
                if (mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text('Erreur lors de la mise à jour: $e'),
                      backgroundColor: Colors.red,
                    ),
                  );
                }
              }
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF4DD0E1),
            ),
            child: const Text('Enregistrer'),
          ),
        ],
      ),
    );

    if (result == true) {
      await _loadProfileData();
    }
  }

  Future<void> _logout() async {
    // Afficher une boîte de dialogue de confirmation
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Déconnexion'),
        content: const Text('Êtes-vous sûr de vouloir vous déconnecter ?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Annuler'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Déconnexion', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );

    if (confirm == true) {
      // Nettoyer les données d'authentification
      await StorageService.clearAuth();
      
      if (mounted) {
        // Naviguer vers la page de login
        Navigator.pushAndRemoveUntil(
          context,
          MaterialPageRoute(builder: (context) => const LoginPage()),
          (route) => false, // Supprimer toutes les routes précédentes
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _loadProfileData,
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                child: Column(
                  children: [
                    // En-tête avec avatar et nom
                    Container(
                      width: double.infinity,
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                          colors: [
                            const Color(0xFF4DD0E1),
                            const Color(0xFF4DD0E1).withOpacity(0.7),
                          ],
                        ),
                      ),
                      child: SafeArea(
                        child: Padding(
                          padding: const EdgeInsets.all(24.0),
                          child: Column(
                            children: [
                              const SizedBox(height: 20),
                              // Avatar
                              CircleAvatar(
                                radius: 50,
                                backgroundColor: Colors.white,
                                child: Text(
                                  _username?.substring(0, 1).toUpperCase() ?? 'U',
                                  style: TextStyle(
                                    fontSize: 40,
                                    fontWeight: FontWeight.bold,
                                    color: const Color(0xFF4DD0E1),
                                  ),
                                ),
                              ),
                              const SizedBox(height: 16),
                              // Nom d'utilisateur
                              Text(
                                _username ?? 'Utilisateur',
                                style: const TextStyle(
                                  fontSize: 24,
                                  fontWeight: FontWeight.bold,
                                  color: Colors.white,
                                ),
                              ),
                              const SizedBox(height: 8),
                              // Niveau et points
                              if (_dashboardData != null)
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    Container(
                                      padding: const EdgeInsets.symmetric(
                                        horizontal: 12,
                                        vertical: 6,
                                      ),
                                      decoration: BoxDecoration(
                                        color: Colors.white.withOpacity(0.2),
                                        borderRadius: BorderRadius.circular(20),
                                      ),
                                      child: Row(
                                        children: [
                                          const Icon(
                                            Icons.star,
                                            color: Colors.white,
                                            size: 18,
                                          ),
                                          const SizedBox(width: 4),
                                          Text(
                                            'Niveau ${_dashboardData!.level}',
                                            style: const TextStyle(
                                              color: Colors.white,
                                              fontWeight: FontWeight.w500,
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                    const SizedBox(width: 12),
                                    Container(
                                      padding: const EdgeInsets.symmetric(
                                        horizontal: 12,
                                        vertical: 6,
                                      ),
                                      decoration: BoxDecoration(
                                        color: Colors.white.withOpacity(0.2),
                                        borderRadius: BorderRadius.circular(20),
                                      ),
                                      child: Row(
                                        children: [
                                          const Icon(
                                            Icons.workspace_premium,
                                            color: Colors.white,
                                            size: 18,
                                          ),
                                          const SizedBox(width: 4),
                                          Text(
                                            '${_dashboardData!.totalPoints} pts',
                                            style: const TextStyle(
                                              color: Colors.white,
                                              fontWeight: FontWeight.w500,
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                  ],
                                ),
                              const SizedBox(height: 20),
                            ],
                          ),
                        ),
                      ),
                    ),

                    // Informations financières
                    Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              const Text(
                                'Informations financières',
                                style: TextStyle(
                                  fontSize: 20,
                                  fontWeight: FontWeight.bold,
                                  color: Colors.black87,
                                ),
                              ),
                              TextButton.icon(
                                onPressed: _showEditFinancialDialog,
                                icon: const Icon(Icons.edit, size: 18),
                                label: const Text('Modifier'),
                              ),
                            ],
                          ),
                          const SizedBox(height: 16),
                          if (_dashboardData != null) ...[
                            _buildInfoCard(
                              icon: Icons.account_balance_wallet,
                              title: 'Revenus totaux',
                              value: '${_dashboardData!.totalIncome.toStringAsFixed(2)} MAD',
                              color: Colors.green,
                            ),
                            const SizedBox(height: 12),
                            _buildInfoCard(
                              icon: Icons.shopping_cart,
                              title: 'Dépenses totales',
                              value: '${_dashboardData!.totalExpenses.toStringAsFixed(2)} MAD',
                              color: Colors.orange,
                            ),
                            const SizedBox(height: 12),
                            _buildInfoCard(
                              icon: Icons.savings,
                              title: 'Épargne totale',
                              value: '${_dashboardData!.totalSavings.toStringAsFixed(2)} MAD',
                              color: Colors.blue,
                            ),
                            const SizedBox(height: 12),
                            _buildInfoCard(
                              icon: Icons.credit_card,
                              title: 'Dette totale',
                              value: '${_dashboardData!.totalDebt.toStringAsFixed(2)} MAD',
                              color: Colors.red,
                            ),
                            const SizedBox(height: 12),
                            _buildInfoCard(
                              icon: Icons.favorite,
                              title: 'Score de santé financière',
                              value: '${_dashboardData!.financialHealthScore.toStringAsFixed(1)}/100',
                              color: _dashboardData!.financialHealthScore >= 70
                                  ? Colors.green
                                  : _dashboardData!.financialHealthScore >= 50
                                      ? Colors.orange
                                      : Colors.red,
                            ),
                          ],
                        ],
                      ),
                    ),

                    // Bouton de déconnexion
                    Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: SizedBox(
                        width: double.infinity,
                        child: ElevatedButton.icon(
                          onPressed: _logout,
                          icon: const Icon(Icons.logout),
                          label: const Text('Se déconnecter'),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.red,
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(vertical: 16),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(12),
                            ),
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(height: 20),
                  ],
                ),
              ),
            ),
    );
  }

  Widget _buildInfoCard({
    required IconData icon,
    required String title,
    required String value,
    required Color color,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: color.withOpacity(0.1),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, color: color, size: 24),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 14,
                    color: Colors.grey,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  value,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.black87,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

