# Endpoints Admin - Documentation

Ce document liste tous les endpoints nécessaires pour l'application d'administration Angular.

## ✅ Endpoints Créés

### 1. Gestion des Utilisateurs (`/api/admin/users`)

- **GET `/api/admin/users`** - Liste paginée des utilisateurs
  - Query params: `page` (default: 0), `size` (default: 10), `search` (optionnel)
  - Retourne: `Page<AdminUserDto>`

- **GET `/api/admin/users/{id}`** - Détails d'un utilisateur
  - Retourne: `AdminUserDetailsDto` avec budgets, dépenses, progression, objectifs

- **PUT `/api/admin/users/{id}/toggle-status`** - Activer/Désactiver un utilisateur
  - Retourne: `200 OK`

- **DELETE `/api/admin/users/{id}`** - Supprimer un utilisateur
  - Retourne: `200 OK`

### 2. Gestion des Cours (`/api/admin/courses`)

- **GET `/api/admin/courses`** - Liste des cours avec filtres
  - Query params: `category`, `language`, `isActive`
  - Retourne: `List<CourseDto>`

- **GET `/api/admin/courses/{id}`** - Détails d'un cours
  - Retourne: `CourseDto` avec quiz

- **POST `/api/admin/courses`** - Créer un nouveau cours
  - Body: `AdminCourseDto`
  - Retourne: `CourseDto`

- **PUT `/api/admin/courses/{id}`** - Modifier un cours
  - Body: `AdminCourseDto`
  - Retourne: `CourseDto`

- **DELETE `/api/admin/courses/{id}`** - Supprimer un cours
  - Retourne: `200 OK`

- **PUT `/api/admin/courses/{id}/toggle-status`** - Activer/Désactiver un cours
  - Retourne: `CourseDto`

### 3. Statistiques (`/api/admin/statistics`)

- **GET `/api/admin/statistics/dashboard`** - Statistiques globales
  - Retourne: `DashboardStatisticsDto`

- **GET `/api/admin/statistics/user-evolution`** - Évolution des utilisateurs
  - Retourne: `List<Map<String, Object>>` avec `date` et `count`

- **GET `/api/admin/statistics/budget-categories`** - Répartition par catégorie
  - Retourne: `List<Map<String, Object>>` avec `category` et `count`

- **GET `/api/admin/statistics/top-users?limit=5`** - Top utilisateurs par points
  - Retourne: `List<Map<String, Object>>` avec `username` et `points`

- **GET `/api/admin/statistics/courses`** - Statistiques par cours
  - Retourne: `List<Map<String, Object>>` avec `courseId`, `courseTitle`, `completions`, `averageScore`

- **GET `/api/admin/statistics/export?format=csv|pdf`** - Export des données
  - Retourne: Fichier à télécharger (à implémenter)

### 4. Notifications (`/api/admin/notifications`)

- **POST `/api/admin/notifications/broadcast`** - Envoyer une notification à tous
  - Body: `NotificationRequest` (title, message)
  - Retourne: `Map` avec `message` et `recipientsCount`

- **GET `/api/admin/notifications/history`** - Historique des notifications
  - Retourne: `List<Map<String, Object>>` avec `id`, `title`, `message`, `sentAt`, `recipientsCount`

## 📝 Modifications Apportées

1. **Modèle User** : Ajout du champ `createdAt` avec `@PrePersist` pour l'initialiser automatiquement

2. **DTOs Créés** :
   - `AdminUserDto` - Pour la liste des utilisateurs
   - `AdminUserDetailsDto` - Pour les détails d'un utilisateur
   - `UserProgressDto` - Pour la progression utilisateur
   - `AdminCourseDto` - Pour créer/modifier un cours
   - `DashboardStatisticsDto` - Pour les statistiques du dashboard
   - `NotificationRequest` - Pour les notifications

3. **Contrôleurs Créés** :
   - `AdminUserController` - Gestion des utilisateurs
   - `AdminCourseController` - Gestion des cours (CRUD)
   - `AdminStatisticsController` - Statistiques
   - `AdminNotificationController` - Notifications système

## 🔒 Sécurité

Tous les endpoints admin nécessitent une authentification JWT. Le token doit être envoyé dans le header :
```
Authorization: Bearer <token>
```

## ⚠️ Notes Importantes

1. **Champ `enabled`** : Actuellement, tous les utilisateurs sont considérés comme activés. Si vous voulez gérer l'activation/désactivation, ajoutez un champ `enabled` dans le modèle `User`.

2. **Recherche utilisateurs** : La recherche est actuellement basique. Pour une meilleure performance, utilisez des requêtes JPA avec `@Query`.

3. **Export CSV/PDF** : L'endpoint d'export retourne actuellement un message. Implémentez la génération de fichiers selon vos besoins.

4. **Évolution des utilisateurs** : L'endpoint retourne une approximation. Pour des données réelles, ajoutez un champ `createdAt` et utilisez des requêtes de groupement par date.

5. **Notifications système** : Les notifications sont créées individuellement pour chaque utilisateur. Pour optimiser, vous pouvez créer une table de notifications système séparée.

## 🚀 Prochaines Étapes

1. Tester tous les endpoints avec Postman ou l'application Angular
2. Ajouter la validation des données (Bean Validation)
3. Implémenter la gestion des erreurs avec des exceptions personnalisées
4. Ajouter des logs pour le suivi des actions admin
5. Implémenter l'export CSV/PDF si nécessaire

