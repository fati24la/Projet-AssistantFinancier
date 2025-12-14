-- Script SQL pour créer l'utilisateur admin directement dans la base de données
-- ATTENTION: Ce script nécessite que vous génériez d'abord le hash BCrypt pour "123456"

-- Étape 1: Générer le hash BCrypt pour "123456"
-- Allez sur https://bcrypt-generator.com/
-- Entrez "123456" et générez le hash
-- Un exemple de hash (rounds: 10): $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- Étape 2: Vérifier si l'utilisateur existe déjà
SELECT * FROM users WHERE username = 'admin';

-- Étape 3: Si aucun résultat, insérer l'utilisateur admin
-- REMPLACEZ le hash ci-dessous par celui que vous avez généré
INSERT INTO users (username, email, password, created_at) 
VALUES (
    'admin', 
    'admin@assistantfinancier.com', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- Hash pour "123456"
    NOW()
);

-- Étape 4: Vérifier que l'utilisateur a été créé
SELECT id, username, email, created_at FROM users WHERE username = 'admin';

