# 🔧 Solution : Erreur Foreign Key Constraint

## ❌ Erreur
```
#1452 - Cannot add or update a child row: a foreign key constraint fails
```

## 🔍 Cause
Cette erreur signifie qu'**aucun utilisateur avec `id=1` n'existe** dans la table `users`. 
La table `user_profiles` a une contrainte de clé étrangère qui exige qu'un utilisateur existe avant de créer son profil.

## ✅ Solutions

### Solution 1 : Créer un Utilisateur d'abord (Recommandé)

**Option A : Via l'application Flutter**
1. Lancez l'application Flutter
2. Allez dans "S'inscrire"
3. Créez un compte avec :
   - Username : `testuser`
   - Email : `test@example.com`
   - Password : `password123`

**Option B : Via SQL directement**
```sql
-- Créer l'utilisateur (mot de passe: password123)
INSERT INTO users (username, email, password) 
VALUES ('testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- Vérifier l'ID créé
SELECT id, username, email FROM users WHERE username = 'testuser';
```

**Puis utilisez l'ID réel dans le script** au lieu de `1`.

### Solution 2 : Utiliser le Script Complet

Utilisez le fichier **`test_data_complete.sql`** qui :
- ✅ Crée automatiquement l'utilisateur si nécessaire
- ✅ Utilise une variable pour l'ID utilisateur
- ✅ Gère tous les doublons
- ✅ Peut être exécuté plusieurs fois

**Instructions :**
1. Ouvrez MySQL Workbench
2. Ouvrez `src/main/resources/test_data_complete.sql`
3. Exécutez le script complet
4. C'est tout ! 🎉

### Solution 3 : Vérifier et Utiliser l'ID Réel

Si vous avez déjà créé un utilisateur via l'application :

```sql
-- Voir tous vos utilisateurs
SELECT id, username, email FROM users;

-- Utiliser l'ID réel (par exemple, si c'est 2 au lieu de 1)
-- Remplacez tous les "1" dans le script par "2"
```

## 📋 Checklist Avant d'Exécuter le Script

- [ ] Un utilisateur existe dans la table `users`
- [ ] Vous connaissez l'ID de cet utilisateur
- [ ] Vous avez remplacé `user_id = 1` par l'ID réel dans le script
- [ ] OU vous utilisez `test_data_complete.sql` qui fait tout automatiquement

## 🎯 Recommandation Finale

**Utilisez `test_data_complete.sql`** - C'est le plus simple et le plus sûr !

Ce script :
- Crée l'utilisateur automatiquement
- Gère tous les doublons
- Peut être exécuté plusieurs fois
- Fonctionne même si les tables sont vides

## 🔍 Vérification

Après avoir exécuté le script, vérifiez :

```sql
-- Vérifier l'utilisateur
SELECT * FROM users;

-- Vérifier le profil
SELECT * FROM user_profiles;

-- Vérifier les budgets
SELECT * FROM budgets;

-- Vérifier les cours
SELECT * FROM courses;
```

