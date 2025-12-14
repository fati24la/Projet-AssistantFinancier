# Vérification de l'utilisateur Admin

## Problème de connexion

Si vous ne pouvez pas vous connecter avec les identifiants admin/123456, suivez ces étapes :

## 1. Vérifier que le backend est démarré

Assurez-vous que le serveur Spring Boot est bien démarré sur le port 8080.

## 2. Vérifier que l'utilisateur admin existe

### Option A: Vérifier dans la console au démarrage

Au démarrage de l'application, vous devriez voir dans la console :
```
🔐 Création de l'utilisateur admin par défaut...
✅ Utilisateur admin créé avec succès !
📝 Identifiants : username='admin', password='123456'
```

OU

```
✅ L'utilisateur admin existe déjà.
```

### Option B: Vérifier dans la base de données

Exécutez cette requête SQL dans votre base de données MySQL :

```sql
SELECT id, username, email, created_at FROM users WHERE username = 'admin';
```

Si aucun résultat, l'utilisateur n'existe pas.

## 3. Créer manuellement l'utilisateur admin

### Option A: Via l'API Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@assistantfinancier.com",
    "password": "123456"
  }'
```

### Option B: Via SQL (nécessite le hash BCrypt)

1. Générez un hash BCrypt pour "123456" sur https://bcrypt-generator.com/
2. Insérez dans la base :

```sql
INSERT INTO users (username, email, password, created_at) 
VALUES ('admin', 'admin@assistantfinancier.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW());
```

### Option C: Redémarrer l'application

L'`AdminInitializer` créera automatiquement l'utilisateur admin s'il n'existe pas.

## 4. Vérifier les logs du backend

Lors de la tentative de connexion, vous devriez voir dans les logs :

```
🔐 [AuthController] Tentative de connexion pour: admin
✅ [AuthController] Utilisateur trouvé: admin
🔑 [AuthController] Vérification du mot de passe...
✅ [AuthController] Mot de passe correct, génération du token...
✅ [AuthController] Token généré avec succès pour: admin
```

Si vous voyez :
```
❌ [AuthController] Utilisateur non trouvé: admin
```
→ L'utilisateur n'existe pas, créez-le.

Si vous voyez :
```
❌ [AuthController] Mot de passe incorrect pour: admin
```
→ Le mot de passe dans la base ne correspond pas à "123456".

## 5. Vérifier la configuration CORS

Assurez-vous que le backend autorise les requêtes depuis `http://localhost:4200`.

## 6. Tester avec Postman/curl

Testez directement l'endpoint de login :

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

Vous devriez recevoir :
```json
{
  "token": "...",
  "username": "admin",
  "userId": 1
}
```

## 7. Vérifier la console du navigateur

Ouvrez la console du navigateur (F12) et vérifiez :
- Les requêtes HTTP vers `http://localhost:8080/api/auth/login`
- Les erreurs CORS éventuelles
- Les messages de log du service AuthService

## Solutions courantes

1. **Base de données vide** : Redémarrez l'application pour que `AdminInitializer` crée l'admin
2. **Mot de passe incorrect** : Vérifiez que le hash dans la base correspond à "123456"
3. **Backend non démarré** : Vérifiez que le serveur tourne sur le port 8080
4. **CORS** : Vérifiez que le backend autorise les requêtes depuis le frontend

