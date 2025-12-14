-- Script SQL pour créer l'utilisateur admin par défaut
-- Mot de passe: 123456 (hashé avec BCrypt)

-- Note: Le hash BCrypt pour "123456" est: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- Vous pouvez générer un nouveau hash avec: https://bcrypt-generator.com/

-- Option 1: Si vous voulez insérer directement (nécessite le hash BCrypt)
-- INSERT INTO users (username, email, password, created_at) 
-- VALUES ('admin', 'admin@assistantfinancier.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW());

-- Option 2: L'utilisateur sera créé automatiquement au démarrage de l'application
-- grâce à AdminInitializer.java

-- Pour vérifier si l'admin existe:
-- SELECT * FROM users WHERE username = 'admin';

