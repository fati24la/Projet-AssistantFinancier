-- ============================================
-- SCRIPT SIMPLE - CRÉER L'UTILISATEUR D'ABORD
-- ============================================
-- Exécutez ce script ÉTAPE PAR ÉTAPE

-- ============================================
-- ÉTAPE 1 : CRÉER L'UTILISATEUR
-- ============================================
-- Mot de passe : password123
INSERT INTO users (username, email, password) 
VALUES ('testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy')
ON DUPLICATE KEY UPDATE username = username;

-- Vérifier l'ID créé
SELECT id, username, email FROM users WHERE username = 'testuser';

-- ============================================
-- ÉTAPE 2 : NOTER L'ID DE L'UTILISATEUR
-- ============================================
-- Remplacez "1" dans les scripts suivants par l'ID réel affiché ci-dessus
-- Par exemple, si l'ID est 2, remplacez tous les "user_id = 1" par "user_id = 2"

-- ============================================
-- ÉTAPE 3 : CRÉER LE PROFIL (Remplacez 1 par l'ID réel)
-- ============================================
INSERT INTO user_profiles (user_id, language, level, points, level_number, monthly_income, monthly_expenses, total_savings, total_debt, financial_goals, created_at, updated_at)
VALUES (1, 'FR', 'INTERMEDIATE', 350, 4, 5000.00, 3500.00, 15000.00, 5000.00, '{"goals": ["Acheter une voiture", "Construire une maison", "Voyager"]}', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    language = 'FR',
    level = 'INTERMEDIATE',
    points = 350,
    level_number = 4,
    monthly_income = 5000.00,
    monthly_expenses = 3500.00,
    total_savings = 15000.00,
    total_debt = 5000.00,
    updated_at = NOW();

-- ============================================
-- ÉTAPE 4 : CRÉER LES BUDGETS (Remplacez 1 par l'ID réel)
-- ============================================
INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
VALUES 
('Budget Alimentation', 'ALIMENTATION', 1500.00, 1200.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1),
('Budget Transport', 'TRANSPORT', 800.00, 650.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1),
('Budget Santé', 'SANTE', 500.00, 200.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1);

-- ============================================
-- ÉTAPE 5 : CRÉER LES DÉPENSES (Remplacez 1 par l'ID réel)
-- ============================================
INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id)
VALUES 
('Courses supermarché', 'ALIMENTATION', 450.00, CURDATE() - INTERVAL 5 DAY, 'CARD', NOW(), 1),
('Restaurant', 'ALIMENTATION', 150.00, CURDATE() - INTERVAL 2 DAY, 'CARD', NOW(), 1),
('Essence', 'TRANSPORT', 300.00, CURDATE() - INTERVAL 3 DAY, 'CARD', NOW(), 1);

-- ============================================
-- ÉTAPE 6 : CRÉER LES OBJECTIFS D'ÉPARGNE (Remplacez 1 par l'ID réel)
-- ============================================
INSERT INTO savings_goals (name, description, target_amount, current_amount, target_date, completed, created_at, updated_at, user_id)
VALUES 
('Achat voiture', 'Économiser pour acheter une voiture d''occasion', 50000.00, 15000.00, DATE_ADD(CURDATE(), INTERVAL 18 MONTH), false, NOW(), NOW(), 1),
('Voyage en Europe', 'Budget pour un voyage de 2 semaines en Europe', 20000.00, 5000.00, DATE_ADD(CURDATE(), INTERVAL 7 MONTH), false, NOW(), NOW(), 1);

-- ============================================
-- ÉTAPE 7 : CRÉER LES COURS (Pas besoin d'user_id)
-- ============================================
INSERT INTO courses (title, description, content, category, difficulty, duration_minutes, language, is_active, created_at)
VALUES 
('Introduction à la gestion budgétaire', 
 'Apprenez les bases de la gestion budgétaire personnelle',
 'La gestion budgétaire est la base de toute bonne santé financière. Dans ce cours, vous apprendrez à créer un budget mensuel, suivre vos dépenses, et économiser efficacement.',
 'BUDGETING', 'BEGINNER', 30, 'FR', true, NOW()),
('Les bases de l''épargne', 
 'Découvrez comment épargner efficacement',
 'L''épargne est essentielle pour votre sécurité financière. Ce cours vous enseignera pourquoi épargner est important et comment fixer des objectifs réalistes.',
 'SAVINGS', 'BEGINNER', 25, 'FR', true, NOW())
ON DUPLICATE KEY UPDATE title = title;

-- ============================================
-- ÉTAPE 8 : CRÉER LES BADGES (Pas besoin d'user_id)
-- ============================================
INSERT INTO badges (name, description, icon, category, requirement)
VALUES 
('Premier pas', 'Vous avez commencé votre parcours financier', '🎯', 'EDUCATION', '{"points": 10}'),
('Épargnant', 'Vous avez économisé régulièrement', '💰', 'SAVINGS', '{"points": 500}'),
('Expert Budget', 'Vous maîtrisez la gestion budgétaire', '📊', 'BUDGET', '{"level": 5}')
ON DUPLICATE KEY UPDATE name = name;

