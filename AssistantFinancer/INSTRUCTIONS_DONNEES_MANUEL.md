# 📝 Instructions pour Ajouter les Données Manuellement

## Configuration

Les données ne sont **PAS** initialisées automatiquement. Les tables sont créées vides au démarrage.

## Option 1 : Utiliser le Script SQL

1. **Ouvrez MySQL Workbench** ou votre outil de gestion MySQL
2. **Sélectionnez votre base de données** : `assistant_financier`
3. **Ouvrez le fichier** : `src/main/resources/test_data.sql`
4. **Exécutez le script** pour insérer les données de test

## Option 2 : Ajouter les Données via l'Application

### Créer un Utilisateur
1. Utilisez l'application Flutter
2. Allez dans "S'inscrire"
3. Créez un compte utilisateur

### Créer des Budgets
1. Connectez-vous à l'application
2. Allez dans la section "Budget"
3. Cliquez sur le bouton "+" pour créer un budget
4. Remplissez le formulaire

### Ajouter des Dépenses
1. Dans la section "Budget"
2. Onglet "Dépenses"
3. Cliquez sur le bouton "+" pour ajouter une dépense

### Créer des Objectifs d'Épargne
1. Via l'API directement (pour l'instant)
2. Ou attendez que l'interface soit implémentée

### Ajouter des Cours
Les cours doivent être ajoutés via SQL ou via l'API backend.

## Script SQL Minimal

Si vous voulez juste créer un utilisateur de test :

```sql
-- Créer un utilisateur de test
-- Le mot de passe est "password123" (hashé avec BCrypt)
INSERT INTO users (username, email, password) 
VALUES ('testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- Créer le profil utilisateur
INSERT INTO user_profiles (user_id, language, level, points, level_number, monthly_income, monthly_expenses, total_savings, total_debt, created_at, updated_at)
SELECT id, 'FR', 'BEGINNER', 0, 1, 0, 0, 0, 0, NOW(), NOW()
FROM users WHERE username = 'testuser';
```

## Configuration de la Base de Données

Dans `application.properties`, vous avez :
```properties
spring.jpa.hibernate.ddl-auto=create
```

- `create` : Recrée les tables à chaque démarrage (⚠️ supprime toutes les données)
- `update` : Met à jour les tables sans supprimer les données (✅ recommandé pour la production)
- `none` : Ne fait rien (vous devez créer les tables manuellement)

**Recommandation** : Changez à `update` si vous voulez conserver vos données :

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Vérifier que les Tables sont Créées

Après le premier démarrage, vérifiez que les tables existent :

```sql
SHOW TABLES;
```

Vous devriez voir :
- users
- user_profiles
- budgets
- expenses
- savings_goals
- courses
- quizzes
- user_progress
- badges
- notifications

## Ajouter des Données Progressivement

Vous pouvez ajouter les données au fur et à mesure :
1. Créez d'abord un utilisateur
2. Créez quelques budgets
3. Ajoutez des dépenses
4. Créez des objectifs d'épargne
5. Ajoutez des cours (via SQL)

Tout est prêt pour que vous ajoutiez les données manuellement ! 🎉

