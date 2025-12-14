# 🔧 Résolution du problème 403 - Démarrage de cours

## Problème
Erreur 403 lors du démarrage d'un cours : "Failed to start course: 403"

## Causes possibles

### 1. Token JWT invalide ou expiré
- Le token peut avoir expiré (durée de vie : 24h)
- Le token peut être mal formaté

### 2. Utilisateur non trouvé dans la base de données
- L'utilisateur connecté n'existe pas dans la table `users`
- Le username dans le token ne correspond à aucun utilisateur

### 3. Problème de configuration Spring Security
- Le filtre JWT ne s'exécute pas correctement
- Le SecurityContext n'est pas correctement rempli

## Solutions

### Solution 1 : Vérifier le token
1. Vérifiez dans les logs du backend si le token est reçu
2. Vérifiez si le username est extrait correctement
3. Vérifiez si l'utilisateur existe dans la base de données

### Solution 2 : Se reconnecter
Si le token a expiré, déconnectez-vous et reconnectez-vous pour obtenir un nouveau token.

### Solution 3 : Vérifier la base de données
Assurez-vous que l'utilisateur existe :
```sql
SELECT * FROM users WHERE username = 'votre_username';
```

### Solution 4 : Vérifier les logs
Regardez les logs du backend Spring Boot pour voir :
- Si le token est reçu
- Si le username est extrait
- Si l'utilisateur est trouvé
- Quelle erreur exacte est levée

## Logs à vérifier

Dans les logs du backend, vous devriez voir :
```
🔐 [JWT Filter] Tentative de validation du token...
✅ [JWT Filter] Token valide pour l'utilisateur: username
✅ [JWT Filter] Authentification définie dans SecurityContext
🎓 [EducationController] Démarrage du cours ID: X
🔐 [UserUtil] Authentication: exists
🔐 [UserUtil] Extracted username: username
✅ [UserUtil] User found with ID: X
```

Si vous voyez des erreurs, notez-les pour le diagnostic.

