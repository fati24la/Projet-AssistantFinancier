# ⚡ Instructions Rapides - Insertion des Données

## 🚨 Erreur "Foreign Key Constraint" ?

Cela signifie qu'**aucun utilisateur n'existe** dans la table `users`.

## ✅ Solution Rapide (3 étapes)

### Étape 1 : Créer un Utilisateur

**Option A : Via l'application Flutter (RECOMMANDÉ)**
1. Lancez l'application Flutter
2. Cliquez sur "S'inscrire"
3. Créez un compte :
   - Username : `testuser`
   - Email : `test@example.com`
   - Password : `password123`

**Option B : Via SQL**
```sql
INSERT INTO users (username, email, password) 
VALUES ('testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');
```

### Étape 2 : Vérifier l'ID de l'utilisateur

```sql
SELECT id, username FROM users WHERE username = 'testuser';
```

**Notez l'ID affiché** (peut être 1, 2, 3, etc.)

### Étape 3 : Utiliser le Bon Script

**Si l'ID est 1 :**
- Utilisez `test_data_complete.sql` (crée tout automatiquement)

**Si l'ID est différent de 1 :**
- Utilisez `test_data_simple.sql` et remplacez tous les `user_id = 1` par `user_id = VOTRE_ID`

## 📁 Fichiers Disponibles

1. **`test_data_complete.sql`** ⭐ **LE PLUS SIMPLE**
   - Crée l'utilisateur automatiquement
   - Utilise une variable pour l'ID
   - Gère tous les doublons
   - **Utilisez celui-ci si possible !**

2. **`test_data_simple.sql`**
   - Script étape par étape
   - Vous devez créer l'utilisateur d'abord
   - Plus de contrôle

3. **`test_data_safe.sql`**
   - Gère les doublons
   - Nécessite que l'utilisateur existe déjà

## 🎯 Recommandation

1. Créez l'utilisateur via l'application Flutter
2. Notez son ID
3. Utilisez `test_data_complete.sql` et modifiez `@user_id` si nécessaire
4. Exécutez le script

## ✅ Vérification

Après l'exécution, vérifiez :

```sql
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_profiles FROM user_profiles;
SELECT COUNT(*) as total_budgets FROM budgets;
SELECT COUNT(*) as total_courses FROM courses;
```

Tout devrait être > 0 ! 🎉

