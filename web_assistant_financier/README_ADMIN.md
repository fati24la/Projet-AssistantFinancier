# Application d'Administration - Assistant Financier

Application web Angular pour l'administration du système "Assistant Financier".

## 🚀 Fonctionnalités

### 1. Authentification Admin
- Page de login (`/login`)
- Stockage du token JWT dans localStorage
- Guard pour protéger les routes admin
- Interceptor HTTP pour ajouter le token automatiquement
- Logout avec nettoyage du token

### 2. Dashboard Admin (`/dashboard`)
- Statistiques globales :
  * Nombre total d'utilisateurs
  * Nombre total de budgets créés
  * Nombre total de dépenses enregistrées
  * Nombre de cours disponibles
  * Nombre de cours complétés
- Graphiques (Chart.js) :
  * Évolution du nombre d'utilisateurs (ligne)
  * Répartition des budgets par catégorie (camembert)
  * Top 5 utilisateurs par points de gamification (barre)

### 3. Gestion des Utilisateurs (`/admin/users`)
- Liste paginée avec recherche et filtres
- Colonnes : ID, Username, Email, Date d'inscription, Statut, Actions
- Actions :
  * Voir détails (modal)
  * Activer/Désactiver compte
  * Supprimer utilisateur
  * Voir les statistiques utilisateur (budgets, dépenses, progression)

### 4. Gestion des Cours (`/admin/courses`)
- Liste des cours avec filtres (catégorie, langue, statut)
- CRUD complet :
  * Créer un nouveau cours
  * Modifier un cours existant
  * Supprimer un cours
  * Activer/Désactiver un cours
- Gestion des quiz pour chaque cours

### 5. Statistiques et Rapports (`/admin/statistics`)
- Vue d'ensemble avec métriques clés
- Graphiques détaillés
- Export des données (CSV, PDF)

### 6. Notifications Système (`/admin/notifications`)
- Envoyer une notification à tous les utilisateurs
- Historique des notifications envoyées
- Modèles de notifications prédéfinis

## 📦 Installation

```bash
cd web_assistant_financier
npm install
```

## 🏃 Démarrage

```bash
npm start
```

L'application sera accessible sur `http://localhost:4200`

## 🔧 Configuration

### API Backend
L'URL de base de l'API est configurée dans les services :
- `http://localhost:8080/api`

Pour modifier l'URL, éditez les fichiers dans `src/app/core/services/`

### Authentification
- Le token JWT est stocké dans `localStorage` avec la clé `admin_token`
- Le guard `authGuard` protège toutes les routes admin
- L'interceptor `jwtInterceptor` ajoute automatiquement le token aux requêtes HTTP

## 📁 Structure du Projet

```
src/app/
├── admin/
│   └── components/
│       ├── login/              # Page de connexion
│       ├── layout/             # Layout admin avec sidebar
│       ├── dashboard/           # Tableau de bord
│       ├── users/               # Gestion des utilisateurs
│       ├── courses/             # Gestion des cours
│       ├── statistics/          # Statistiques détaillées
│       └── notifications/       # Notifications système
├── core/
│   ├── guards/                  # Guards d'authentification
│   ├── interceptors/           # Interceptors HTTP
│   └── services/                # Services (Auth, User, Course, etc.)
└── models/                      # Interfaces TypeScript
```

## 🔐 Endpoints API Requis

L'application nécessite les endpoints suivants dans le backend :

### Authentification
- `POST /api/auth/login` - Connexion admin

### Utilisateurs
- `GET /api/admin/users` - Liste tous les utilisateurs
- `GET /api/admin/users/{id}` - Détails d'un utilisateur
- `PUT /api/admin/users/{id}/toggle-status` - Activer/Désactiver
- `DELETE /api/admin/users/{id}` - Supprimer

### Statistiques
- `GET /api/admin/statistics/dashboard` - Statistiques globales
- `GET /api/admin/statistics/user-evolution` - Évolution des utilisateurs
- `GET /api/admin/statistics/budget-categories` - Répartition budgets
- `GET /api/admin/statistics/top-users?limit=5` - Top utilisateurs
- `GET /api/admin/statistics/courses` - Statistiques cours
- `GET /api/admin/statistics/export?format=csv|pdf` - Export données

### Cours
- `GET /api/admin/courses` - Liste des cours
- `GET /api/admin/courses/{id}` - Détails d'un cours
- `POST /api/admin/courses` - Créer un cours
- `PUT /api/admin/courses/{id}` - Modifier un cours
- `DELETE /api/admin/courses/{id}` - Supprimer un cours
- `PUT /api/admin/courses/{id}/toggle-status` - Activer/Désactiver

### Notifications
- `POST /api/admin/notifications/broadcast` - Envoyer notification
- `GET /api/admin/notifications/history` - Historique

## 🎨 Technologies Utilisées

- **Angular 20** - Framework frontend
- **Angular Material** - Composants UI
- **Chart.js / ng2-charts** - Graphiques
- **RxJS** - Programmation réactive
- **TypeScript** - Langage de programmation
- **Reactive Forms** - Formulaires

## 📝 Notes

- L'application utilise le mode strict de TypeScript
- Tous les composants sont standalone
- Les interceptors gèrent automatiquement l'ajout du token JWT
- Les erreurs HTTP 401 redirigent automatiquement vers la page de login

## 🐛 Dépannage

### Erreur CORS
Assurez-vous que le backend autorise les requêtes depuis `http://localhost:4200`

### Token expiré
L'interceptor d'erreur redirige automatiquement vers `/login` en cas d'erreur 401

### Graphiques ne s'affichent pas
Vérifiez que les données sont retournées par l'API dans le format attendu

