-- ============================================
-- CRÉER L'UTILISATEUR EN PREMIER
-- ============================================
-- Exécutez ce script AVANT test_data_complete.sql
-- Mot de passe : password123

INSERT INTO users (username, email, password) 
VALUES ('testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy')
ON DUPLICATE KEY UPDATE username = username;

-- Vérifier que l'utilisateur a été créé
SELECT id, username, email FROM users WHERE username = 'testuser';

-- Notez l'ID affiché ci-dessus
-- Si l'ID n'est pas 1, vous devrez modifier test_data_complete.sql
-- et remplacer @user_id par l'ID réel, ou utiliser test_data_simple.sql

