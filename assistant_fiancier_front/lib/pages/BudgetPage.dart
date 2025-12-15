import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/budget.dart';
import '../models/expense.dart';
import '../models/savings_goal.dart';
import '../services/budget_service.dart';
import '../services/savings_goal_service.dart';

class BudgetPage extends StatefulWidget {
  const BudgetPage({Key? key}) : super(key: key);

  @override
  State<BudgetPage> createState() => _BudgetPageState();
}

class _BudgetPageState extends State<BudgetPage>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  List<Budget> _budgets = [];
  List<Expense> _expenses = [];
  List<SavingsGoal> _goals = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _loadData();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);
    try {
      final budgets = await BudgetService.getBudgets();
      final expenses = await BudgetService.getExpenses();
      final goals = await SavingsGoalService.getGoals();
      setState(() {
        _budgets = budgets;
        _expenses = expenses;
        _goals = goals;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: const Text('Gestion du Budget'),
        backgroundColor: const Color(0xFF4DD0E1),
        bottom: TabBar(
          controller: _tabController,
          labelColor: Colors.white,
          unselectedLabelColor: Colors.white70,
          indicatorColor: Colors.white,
          tabs: const [
            Tab(text: 'Budgets', icon: Icon(Icons.account_balance_wallet)),
            Tab(text: 'Dépenses', icon: Icon(Icons.receipt)),
            Tab(text: 'Objectifs', icon: Icon(Icons.savings)),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () {
              if (_tabController.index == 0) {
                _showAddBudgetDialog();
              } else if (_tabController.index == 1) {
                _showAddExpenseDialog();
              } else {
                _showAddSavingsGoalDialog();
              }
            },
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _loadData,
              child: TabBarView(
                controller: _tabController,
                children: [
                  _buildBudgetsTab(),
                  _buildExpensesTab(),
                  _buildSavingsGoalsTab(),
                ],
              ),
            ),
      floatingActionButton: null,
    );
  }

  Widget _buildBudgetsTab() {
    if (_budgets.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.account_balance_wallet, size: 64, color: Colors.grey[400]),
            const SizedBox(height: 16),
            Text(
              'Aucun budget créé',
              style: TextStyle(fontSize: 18, color: Colors.grey[600]),
            ),
            const SizedBox(height: 8),
            ElevatedButton.icon(
              onPressed: () => _showAddBudgetDialog(),
              icon: const Icon(Icons.add),
              label: const Text('Créer un budget'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4DD0E1),
              ),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _budgets.length,
      itemBuilder: (context, index) {
        final budget = _budgets[index];
        return _buildBudgetCard(budget);
      },
    );
  }

  Widget _buildBudgetCard(Budget budget) {
    final percentage = budget.percentageUsed ?? (budget.spent / budget.amount * 100);
    final isOverBudget = percentage > 100;
    final remaining = budget.remaining;

    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      budget.name,
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      budget.category,
                      style: TextStyle(
                        fontSize: 14,
                        color: Colors.grey[600],
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: isOverBudget ? Colors.red.withOpacity(0.1) : Colors.green.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  '${percentage.toStringAsFixed(0)}%',
                  style: TextStyle(
                    color: isOverBudget ? Colors.red : Colors.green,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          LinearProgressIndicator(
            value: (percentage / 100).clamp(0.0, 1.0),
            backgroundColor: Colors.grey[200],
            valueColor: AlwaysStoppedAnimation<Color>(
              isOverBudget ? Colors.red : const Color(0xFF4DD0E1),
            ),
            minHeight: 8,
          ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Dépensé',
                    style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${budget.spent.toStringAsFixed(0)} MAD',
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    'Restant',
                    style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${remaining.toStringAsFixed(0)} MAD',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      color: isOverBudget ? Colors.red : Colors.green,
                    ),
                  ),
                ],
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    'Total',
                    style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${budget.amount.toStringAsFixed(0)} MAD',
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildExpensesTab() {
    if (_expenses.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.receipt, size: 64, color: Colors.grey[400]),
            const SizedBox(height: 16),
            Text(
              'Aucune dépense enregistrée',
              style: TextStyle(fontSize: 18, color: Colors.grey[600]),
            ),
            const SizedBox(height: 8),
            ElevatedButton.icon(
              onPressed: () => _showAddExpenseDialog(),
              icon: const Icon(Icons.add),
              label: const Text('Ajouter une dépense'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4DD0E1),
              ),
            ),
          ],
        ),
      );
    }

    // Grouper les dépenses par date
    final groupedExpenses = <String, List<Expense>>{};
    for (var expense in _expenses) {
      final date = expense.date.split('T')[0];
      groupedExpenses.putIfAbsent(date, () => []).add(expense);
    }

    final sortedDates = groupedExpenses.keys.toList()..sort((a, b) => b.compareTo(a));

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: sortedDates.length,
      itemBuilder: (context, index) {
        final date = sortedDates[index];
        final expenses = groupedExpenses[date]!;
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 8),
              child: Text(
                _formatDate(date),
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.bold,
                  color: Colors.grey[600],
                ),
              ),
            ),
            ...expenses.map((expense) => _buildExpenseCard(expense)),
            const SizedBox(height: 8),
          ],
        );
      },
    );
  }

  Widget _buildSavingsGoalsTab() {
    if (_goals.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.savings, size: 64, color: Colors.grey[400]),
            const SizedBox(height: 16),
            Text(
              'Aucun objectif d\'épargne',
              style: TextStyle(fontSize: 18, color: Colors.grey[600]),
            ),
            const SizedBox(height: 8),
            ElevatedButton.icon(
              onPressed: () => _showAddSavingsGoalDialog(),
              icon: const Icon(Icons.add),
              label: const Text('Créer un objectif'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4DD0E1),
              ),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _goals.length,
      itemBuilder: (context, index) {
        final goal = _goals[index];
        return _buildSavingsGoalCard(goal);
      },
    );
  }

  Widget _buildSavingsGoalCard(SavingsGoal goal) {
    final progress = (goal.progressPercentage / 100).clamp(0.0, 1.0);
    final isCompleted = goal.completed || progress >= 1.0;

    return GestureDetector(
      onTap: isCompleted ? null : () => _showAddAmountToGoalDialog(goal),
      child: Container(
        margin: const EdgeInsets.only(bottom: 16),
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.05),
              blurRadius: 10,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        goal.name,
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        goal.description,
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.grey[600],
                        ),
                      ),
                    ],
                  ),
                ),
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: isCompleted
                        ? Colors.green.withOpacity(0.1)
                        : const Color(0xFF4DD0E1).withOpacity(0.1),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    '${goal.progressPercentage.toStringAsFixed(0)}%',
                    style: TextStyle(
                      color: isCompleted ? Colors.green : const Color(0xFF4DD0E1),
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            LinearProgressIndicator(
              value: progress,
              backgroundColor: Colors.grey[200],
              valueColor: AlwaysStoppedAnimation<Color>(
                isCompleted ? Colors.green : const Color(0xFF4DD0E1),
              ),
              minHeight: 8,
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Épargné',
                      style:
                          TextStyle(fontSize: 12, color: Colors.grey[600]),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '${goal.currentAmount.toStringAsFixed(0)} MAD',
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      'Restant',
                      style:
                          TextStyle(fontSize: 12, color: Colors.grey[600]),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '${goal.remainingAmount.toStringAsFixed(0)} MAD',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: isCompleted ? Colors.green : Colors.orange,
                      ),
                    ),
                  ],
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      'Objectif',
                      style:
                          TextStyle(fontSize: 12, color: Colors.grey[600]),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '${goal.targetAmount.toStringAsFixed(0)} MAD',
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    const Icon(Icons.event, size: 16, color: Colors.grey),
                    const SizedBox(width: 4),
                    Text(
                      'Avant le ${goal.targetDate}',
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.grey[600],
                      ),
                    ),
                  ],
                ),
                if (!isCompleted)
                  Text(
                    '${goal.daysRemaining} jours restants',
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.grey[600],
                    ),
                  )
                else
                  Text(
                    'Objectif atteint 🎉',
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.green[700],
                      fontWeight: FontWeight.w600,
                    ),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildExpenseCard(Expense expense) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 5,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: _getCategoryColor(expense.category).withOpacity(0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(
              _getCategoryIcon(expense.category),
              color: _getCategoryColor(expense.category),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  expense.description,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 4),
                Row(
                  children: [
                    Text(
                      expense.category,
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.grey[600],
                      ),
                    ),
                    const SizedBox(width: 8),
                    Icon(Icons.circle, size: 4, color: Colors.grey[400]),
                    const SizedBox(width: 8),
                    Text(
                      expense.paymentMethod,
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.grey[600],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          Text(
            '${expense.amount.toStringAsFixed(0)} MAD',
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: Colors.red,
            ),
          ),
        ],
      ),
    );
  }

  Color _getCategoryColor(String category) {
    switch (category.toUpperCase()) {
      case 'ALIMENTATION':
        return Colors.orange;
      case 'TRANSPORT':
        return Colors.blue;
      case 'SANTE':
        return Colors.red;
      case 'EDUCATION':
        return Colors.purple;
      case 'LOISIRS':
        return Colors.green;
      default:
        return const Color(0xFF4DD0E1);
    }
  }

  IconData _getCategoryIcon(String category) {
    switch (category.toUpperCase()) {
      case 'ALIMENTATION':
        return Icons.restaurant;
      case 'TRANSPORT':
        return Icons.directions_car;
      case 'SANTE':
        return Icons.medical_services;
      case 'EDUCATION':
        return Icons.school;
      case 'LOISIRS':
        return Icons.sports_esports;
      default:
        return Icons.category;
    }
  }

  String _formatDate(String date) {
    try {
      final parsed = DateTime.parse(date);
      final now = DateTime.now();
      if (parsed.year == now.year && parsed.month == now.month && parsed.day == now.day) {
        return 'Aujourd\'hui';
      } else if (parsed.year == now.year && parsed.month == now.month && parsed.day == now.day - 1) {
        return 'Hier';
      } else {
        return DateFormat('dd MMMM yyyy', 'fr').format(parsed);
      }
    } catch (e) {
      return date;
    }
  }

  void _showAddBudgetDialog() {
    final nameController = TextEditingController();
    final amountController = TextEditingController();
    String selectedCategory = 'ALIMENTATION';
    final categories = ['ALIMENTATION', 'TRANSPORT', 'SANTE', 'EDUCATION', 'LOISIRS'];

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setState) => AlertDialog(
          title: const Text('Nouveau Budget'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: nameController,
                  decoration: const InputDecoration(
                    labelText: 'Nom du budget',
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<String>(
                  value: selectedCategory,
                  decoration: const InputDecoration(
                    labelText: 'Catégorie',
                    border: OutlineInputBorder(),
                  ),
                  items: categories.map((cat) {
                    return DropdownMenuItem(
                      value: cat,
                      child: Text(cat),
                    );
                  }).toList(),
                  onChanged: (value) {
                    setState(() => selectedCategory = value!);
                  },
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: amountController,
                  decoration: const InputDecoration(
                    labelText: 'Montant (MAD)',
                    border: OutlineInputBorder(),
                  ),
                  keyboardType: TextInputType.number,
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Annuler'),
            ),
            ElevatedButton(
              onPressed: () async {
                if (nameController.text.isEmpty || amountController.text.isEmpty) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Veuillez remplir tous les champs')),
                  );
                  return;
                }

                try {
                  final now = DateTime.now();
                  final budget = Budget(
                    name: nameController.text,
                    category: selectedCategory,
                    amount: double.parse(amountController.text),
                    spent: 0,
                    remaining: double.parse(amountController.text),
                    startDate: DateTime(now.year, now.month, 1).toIso8601String().split('T')[0],
                    endDate: DateTime(now.year, now.month + 1, 0).toIso8601String().split('T')[0],
                    period: 'MONTHLY',
                  );

                  await BudgetService.createBudget(budget);
                  Navigator.pop(context);
                  _loadData();
                } catch (e) {
                  if (mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Erreur: $e')),
                    );
                  }
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4DD0E1),
              ),
              child: const Text('Créer'),
            ),
          ],
        ),
      ),
    );
  }

  void _showAddExpenseDialog() {
    final descriptionController = TextEditingController();
    final amountController = TextEditingController();
    String selectedCategory = 'ALIMENTATION';
    String selectedPaymentMethod = 'CARD';
    final categories = ['ALIMENTATION', 'TRANSPORT', 'SANTE', 'EDUCATION', 'LOISIRS'];
    final paymentMethods = ['CARD', 'CASH', 'MOBILE_MONEY'];

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setState) => AlertDialog(
          title: const Text('Nouvelle Dépense'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: descriptionController,
                  decoration: const InputDecoration(
                    labelText: 'Description',
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<String>(
                  value: selectedCategory,
                  decoration: const InputDecoration(
                    labelText: 'Catégorie',
                    border: OutlineInputBorder(),
                  ),
                  items: categories.map((cat) {
                    return DropdownMenuItem(
                      value: cat,
                      child: Text(cat),
                    );
                  }).toList(),
                  onChanged: (value) {
                    setState(() => selectedCategory = value!);
                  },
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: amountController,
                  decoration: const InputDecoration(
                    labelText: 'Montant (MAD)',
                    border: OutlineInputBorder(),
                  ),
                  keyboardType: TextInputType.number,
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<String>(
                  value: selectedPaymentMethod,
                  decoration: const InputDecoration(
                    labelText: 'Méthode de paiement',
                    border: OutlineInputBorder(),
                  ),
                  items: paymentMethods.map((method) {
                    return DropdownMenuItem(
                      value: method,
                      child: Text(method),
                    );
                  }).toList(),
                  onChanged: (value) {
                    setState(() => selectedPaymentMethod = value!);
                  },
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Annuler'),
            ),
            ElevatedButton(
              onPressed: () async {
                if (descriptionController.text.isEmpty || amountController.text.isEmpty) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Veuillez remplir tous les champs')),
                  );
                  return;
                }

                try {
                  final expense = Expense(
                    description: descriptionController.text,
                    category: selectedCategory,
                    amount: double.parse(amountController.text),
                    date: DateTime.now().toIso8601String().split('T')[0],
                    paymentMethod: selectedPaymentMethod,
                  );

                  await BudgetService.createExpense(expense);
                  Navigator.pop(context);
                  _loadData();
                } catch (e) {
                  if (mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Erreur: $e')),
                    );
                  }
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4DD0E1),
              ),
              child: const Text('Ajouter'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _showAddSavingsGoalDialog() async {
    final nameController = TextEditingController();
    final descriptionController = TextEditingController();
    final amountController = TextEditingController();
    DateTime? selectedDate;

    await showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setState) => AlertDialog(
          title: const Text('Nouvel objectif d\'épargne'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: nameController,
                  decoration: const InputDecoration(
                    labelText: 'Nom de l\'objectif',
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: descriptionController,
                  decoration: const InputDecoration(
                    labelText: 'Description',
                    border: OutlineInputBorder(),
                  ),
                  maxLines: 2,
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: amountController,
                  decoration: const InputDecoration(
                    labelText: 'Montant cible (MAD)',
                    border: OutlineInputBorder(),
                  ),
                  keyboardType: TextInputType.number,
                ),
                const SizedBox(height: 16),
                InkWell(
                  onTap: () async {
                    final now = DateTime.now();
                    final picked = await showDatePicker(
                      context: context,
                      initialDate: now.add(const Duration(days: 30)),
                      firstDate: now,
                      lastDate: DateTime(now.year + 10),
                    );
                    if (picked != null) {
                      setState(() {
                        selectedDate = picked;
                      });
                    }
                  },
                  child: InputDecorator(
                    decoration: const InputDecoration(
                      labelText: 'Date cible',
                      border: OutlineInputBorder(),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          selectedDate == null
                              ? 'Sélectionner une date'
                              : DateFormat('dd/MM/yyyy').format(selectedDate!),
                          style: TextStyle(
                            color: selectedDate == null
                                ? Colors.grey[600]
                                : Colors.black87,
                          ),
                        ),
                        const Icon(Icons.calendar_today, size: 18),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Annuler'),
            ),
            ElevatedButton(
              onPressed: () async {
                if (nameController.text.isEmpty ||
                    amountController.text.isEmpty ||
                    selectedDate == null) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('Veuillez remplir tous les champs obligatoires'),
                    ),
                  );
                  return;
                }

                try {
                  final goal = SavingsGoal(
                    name: nameController.text,
                    description: descriptionController.text,
                    targetAmount: double.parse(amountController.text),
                    currentAmount: 0,
                    remainingAmount: double.parse(amountController.text),
                    progressPercentage: 0,
                    targetDate:
                        selectedDate!.toIso8601String().split('T')[0],
                    daysRemaining: 0,
                    completed: false,
                  );

                  await SavingsGoalService.createGoal(goal);
                  if (mounted) {
                    Navigator.pop(context);
                    _loadData();
                  }
                } catch (e) {
                  if (mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Erreur: $e')),
                    );
                  }
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4DD0E1),
              ),
              child: const Text('Créer'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _showAddAmountToGoalDialog(SavingsGoal goal) async {
    final amountController = TextEditingController();

    await showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Ajouter à "${goal.name}"'),
        content: TextField(
          controller: amountController,
          decoration: const InputDecoration(
            labelText: 'Montant à ajouter (MAD)',
            border: OutlineInputBorder(),
          ),
          keyboardType: TextInputType.number,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler'),
          ),
          ElevatedButton(
            onPressed: () async {
              if (amountController.text.isEmpty) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Veuillez indiquer un montant'),
                  ),
                );
                return;
              }

              try {
                final amount = double.parse(amountController.text);
                await SavingsGoalService.addToGoal(goal.id!, amount);
                if (mounted) {
                  Navigator.pop(context);
                  _loadData();
                }
              } catch (e) {
                if (mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Erreur: $e')),
                  );
                }
              }
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF4DD0E1),
            ),
            child: const Text('Ajouter'),
          ),
        ],
      ),
    );
  }
}

