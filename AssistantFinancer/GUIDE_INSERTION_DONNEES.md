# 📋 Guide d'Insertion des Données Manuellement

## ⚠️ Problème de Doublon Résolu

Si vous avez l'erreur **"Duplicate entry"**, c'est que des données existent déjà. Voici les solutions :

## 🔧 Solution 1 : Utiliser le Script Sécurisé (Recommandé)

Utilisez le fichier **`test_data_safe.sql`** qui gère automatiquement les doublons :

1. Ouvrez MySQL Workbench
2. Sélectionnez votre base de données : `assistant_financier`
3. Ouvrez le fichier : `src/main/resources/test_data_safe.sql`
4. Exécutez le script

Ce script vérifie l'existence des données avant d'insérer, donc vous pouvez l'exécuter plusieurs fois sans erreur.

## 🔧 Solution 2 : Supprimer les Données Existantes

Si vous voulez repartir de zéro, exécutez d'abord ces commandes :

```sql
-- Supprimer les données existantes pour user_id = 1
DELETE FROM expenses WHERE user_id = 1;
DELETE FROM budgets WHERE user_id = 1;
DELETE FROM savings_goals WHERE user_id = 1;
DELETE FROM user_progress WHERE user_id = 1;
DELETE FROM notifications WHERE user_id = 1;
DELETE FROM user_badges WHERE user_id = 1;
DELETE FROM user_profiles WHERE user_id = 1;
```

Puis exécutez le script `test_data.sql` normal.

## 🔧 Solution 3 : Mettre à Jour le Profil Existant

Si vous voulez juste mettre à jour le profil utilisateur existant :

```sql
UPDATE user_profiles 
SET 
    language = 'FR',
    level = 'INTERMEDIATE',
    points = 350,
    level_number = 4,
    monthly_income = 5000.00,
    monthly_expenses = 3500.00,
    total_savings = 15000.00,
    total_debt = 5000.00,
    updated_at = NOW()
WHERE user_id = 1;
```

## 📝 Fichiers Disponibles

1. **`test_data.sql`** - Script original (peut causer des erreurs de doublon)
2. **`test_data_safe.sql`** - Script sécurisé (gère les doublons) ⭐ **RECOMMANDÉ**
3. **`test_data_clean.sql`** - Script avec option de nettoyage

## ✅ Vérifier Votre Utilisateur

Avant d'exécuter le script, vérifiez que vous avez un utilisateur :

```sql
SELECT * FROM users WHERE id = 1;
```

Si aucun utilisateur n'existe, créez-en un via l'application Flutter (inscription) ou créez-le manuellement.

## 🎯 Recommandation

**Utilisez `test_data_safe.sql`** - C'est le plus sûr et vous pouvez l'exécuter plusieurs fois sans problème !

