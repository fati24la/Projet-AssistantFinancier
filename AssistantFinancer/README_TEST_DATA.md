# 📊 Guide d'Insertion des Données de Test

Ce guide vous explique comment insérer les données de test dans votre base de données pour tester toutes les nouvelles fonctionnalités.

## 🚀 Méthode 1 : Via MySQL Workbench / phpMyAdmin

1. **Ouvrez votre outil de gestion MySQL** (Workbench, phpMyAdmin, etc.)
2. **Sélectionnez votre base de données** : `assistant_financier`
3. **Ouvrez le fichier** `src/main/resources/test_data.sql`
4. **Exécutez le script** complet

## 🚀 Méthode 2 : Via la ligne de commande MySQL

```bash
# Se connecter à MySQL
mysql -u root -p

# Sélectionner la base de données
USE assistant_financier;

# Exécuter le script
SOURCE C:/Users/abdelillah/Desktop/emsi_s9/projects/Projet-AssistantFinancier/AssistantFinancer/src/main/resources/test_data.sql;
```

## 🚀 Méthode 3 : Via l'application Spring Boot

Vous pouvez créer un endpoint temporaire ou utiliser un `@PostConstruct` dans une classe de configuration pour insérer les données au démarrage.

## ⚠️ Prérequis IMPORTANTS

### 1. Vérifier que l'utilisateur existe

Avant d'exécuter le script, assurez-vous qu'un utilisateur avec `id = 1` existe dans la table `users`. Si ce n'est pas le cas :

```sql
-- Vérifier les utilisateurs existants
SELECT * FROM users;

-- Si aucun utilisateur avec id=1, créez-en un via l'application
-- ou insérez-en un manuellement :
INSERT INTO users (username, email, password) 
VALUES ('testuser', 'test@example.com', '$2a$10$...'); -- Utilisez un hash BCrypt valide
```

### 2. Ajuster les IDs de budgets

Les dépenses référencent des budgets. Si les IDs générés automatiquement sont différents, ajustez les `budget_id` dans la section EXPENSES.

## 📋 Données Incluses

Le script insère :

### ✅ Profil Utilisateur
- Points : 350
- Niveau : 4 (INTERMEDIATE)
- Revenus mensuels : 5000 MAD
- Dépenses mensuelles : 3500 MAD
- Épargne totale : 15000 MAD
- Dette totale : 5000 MAD

### ✅ Budgets (5 budgets)
- Alimentation : 1500 MAD (1200 dépensés)
- Transport : 800 MAD (650 dépensés)
- Santé : 500 MAD (200 dépensés)
- Éducation : 300 MAD (150 dépensés)
- Loisirs : 400 MAD (300 dépensés)

### ✅ Dépenses (15+ dépenses)
- Réparties sur janvier 2024
- Avec différentes catégories et méthodes de paiement

### ✅ Objectifs d'épargne (4 objectifs)
- Achat voiture : 50000 MAD (15000 épargnés)
- Voyage Europe : 20000 MAD (5000 épargnés)
- Fonds d'urgence : 15000 MAD (12000 épargnés)
- Équipement maison : 10000 MAD (10000 épargnés) - ✅ COMPLÉTÉ

### ✅ Badges (5 badges)
- Premier pas, Épargnant, Expert Budget, etc.
- 2 badges déjà attribués à l'utilisateur

### ✅ Cours (5 cours)
- Introduction à la gestion budgétaire
- Les bases de l'épargne
- Comprendre le crédit
- L'inclusion bancaire
- Introduction aux investissements

### ✅ Quiz (8 questions)
- Réparties sur les différents cours

### ✅ Progression (3 cours)
- 2 cours complétés avec scores
- 1 cours en cours

### ✅ Notifications (5 notifications)
- Alertes de budget
- Objectifs atteints
- Conseils éducatifs
- Rappels

## 🧪 Tester les Fonctionnalités

Après avoir inséré les données :

### 1. Dashboard
- Connectez-vous à l'application
- Vous devriez voir :
  - Score de santé financière calculé
  - Graphiques revenus/dépenses
  - Dépenses par catégorie
  - Objectifs d'épargne actifs
  - Points et niveau

### 2. Budget
- Naviguez vers la section Budget
- Vous verrez vos 5 budgets avec les pourcentages utilisés

### 3. Éducation
- Naviguez vers la section Éducation
- Vous verrez les cours disponibles
- Vous pouvez démarrer/compléter des cours

### 4. Assistant
- Utilisez l'assistant vocal pour poser des questions
- L'IA peut maintenant accéder à votre profil et historique

## 🔍 Requêtes de Vérification

```sql
-- Vérifier le profil utilisateur
SELECT * FROM user_profiles WHERE user_id = 1;

-- Vérifier les budgets
SELECT name, category, amount, spent, 
       (spent/amount)*100 as percentage_used 
FROM budgets WHERE user_id = 1;

-- Vérifier les dépenses par catégorie
SELECT category, SUM(amount) as total 
FROM expenses 
WHERE user_id = 1 
GROUP BY category;

-- Vérifier les objectifs d'épargne
SELECT name, current_amount, target_amount, 
       (current_amount/target_amount)*100 as progress 
FROM savings_goals 
WHERE user_id = 1;

-- Vérifier les cours complétés
SELECT c.title, up.score, up.completed 
FROM user_progress up
JOIN courses c ON up.course_id = c.id
WHERE up.user_id = 1;

-- Vérifier les notifications non lues
SELECT title, message, type 
FROM notifications 
WHERE user_id = 1 AND is_read = false;
```

## 🛠️ Personnaliser les Données

Vous pouvez modifier les données selon vos besoins :

- **Changer les montants** : Modifiez les valeurs dans les INSERT
- **Changer les dates** : Ajustez les dates pour tester différentes périodes
- **Ajouter plus de données** : Dupliquez les INSERT avec de nouvelles valeurs
- **Changer l'utilisateur** : Remplacez `user_id = 1` par votre ID utilisateur

## ⚠️ Note Importante

Si vous utilisez `spring.jpa.hibernate.ddl-auto=create`, les tables seront recréées à chaque démarrage et vous devrez réinsérer les données. Pour éviter cela, changez à `update` :

```properties
spring.jpa.hibernate.ddl-auto=update
```

## 🎯 Prochaines Étapes

1. Insérez les données de test
2. Démarrez l'application Spring Boot
3. Démarrez l'application Flutter
4. Connectez-vous avec votre compte
5. Explorez toutes les nouvelles fonctionnalités !

