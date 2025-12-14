-- ============================================
-- SCRIPT DE NETTOYAGE ET INSERTION SÉCURISÉE
-- ============================================
-- Ce script supprime d'abord les données existantes, puis insère les nouvelles
-- UTILISEZ AVEC PRUDENCE - Cela supprimera toutes vos données de test !

-- ============================================
-- ÉTAPE 1 : NETTOYER LES DONNÉES EXISTANTES
-- ============================================
-- Décommentez les lignes ci-dessous si vous voulez supprimer les données existantes
-- ATTENTION : Cela supprimera TOUTES les données pour user_id = 1

-- DELETE FROM expenses WHERE user_id = 1;
-- DELETE FROM budgets WHERE user_id = 1;
-- DELETE FROM savings_goals WHERE user_id = 1;
-- DELETE FROM user_progress WHERE user_id = 1;
-- DELETE FROM notifications WHERE user_id = 1;
-- DELETE FROM user_badges WHERE user_id = 1;
-- DELETE FROM user_profiles WHERE user_id = 1;

-- ============================================
-- ÉTAPE 2 : VÉRIFIER QUE L'UTILISATEUR EXISTE
-- ============================================
-- Assurez-vous qu'un utilisateur avec id=1 existe
-- Si ce n'est pas le cas, créez-le d'abord via l'application ou avec cette commande :
-- 
-- INSERT INTO users (username, email, password) 
-- VALUES ('testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');
-- (Le mot de passe est "password123")

-- ============================================
-- ÉTAPE 3 : INSÉRER LES DONNÉES (VERSION SÉCURISÉE)
-- ============================================

-- 1. USER PROFILE
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

-- 2. BUDGETS
INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
VALUES 
('Budget Alimentation', 'ALIMENTATION', 1500.00, 1200.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1),
('Budget Transport', 'TRANSPORT', 800.00, 650.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1),
('Budget Santé', 'SANTE', 500.00, 200.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1),
('Budget Éducation', 'EDUCATION', 300.00, 150.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1),
('Budget Loisirs', 'LOISIRS', 400.00, 300.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1)
ON DUPLICATE KEY UPDATE
    amount = VALUES(amount),
    spent = VALUES(spent),
    updated_at = NOW();

-- 3. EXPENSES (Simplifié - sans budget_id pour éviter les erreurs)
INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id)
VALUES 
('Courses supermarché', 'ALIMENTATION', 450.00, CURDATE() - INTERVAL 5 DAY, 'CARD', NOW(), 1),
('Restaurant', 'ALIMENTATION', 150.00, CURDATE() - INTERVAL 2 DAY, 'CARD', NOW(), 1),
('Essence', 'TRANSPORT', 300.00, CURDATE() - INTERVAL 3 DAY, 'CARD', NOW(), 1),
('Consultation médicale', 'SANTE', 150.00, CURDATE() - INTERVAL 1 DAY, 'CARD', NOW(), 1),
('Cinéma', 'LOISIRS', 50.00, CURDATE(), 'CARD', NOW(), 1);

-- 4. SAVINGS GOALS
INSERT INTO savings_goals (name, description, target_amount, current_amount, target_date, completed, created_at, updated_at, user_id)
VALUES 
('Achat voiture', 'Économiser pour acheter une voiture d''occasion', 50000.00, 15000.00, DATE_ADD(CURDATE(), INTERVAL 18 MONTH), false, NOW(), NOW(), 1),
('Voyage en Europe', 'Budget pour un voyage de 2 semaines en Europe', 20000.00, 5000.00, DATE_ADD(CURDATE(), INTERVAL 7 MONTH), false, NOW(), NOW(), 1),
('Fonds d''urgence', 'Créer un fonds d''urgence de 3 mois de salaire', 15000.00, 12000.00, DATE_ADD(CURDATE(), INTERVAL 3 MONTH), false, NOW(), NOW(), 1);

-- 5. BADGES
INSERT INTO badges (name, description, icon, category, requirement)
VALUES 
('Premier pas', 'Vous avez commencé votre parcours financier', '🎯', 'EDUCATION', '{"points": 10}'),
('Épargnant', 'Vous avez économisé régulièrement', '💰', 'SAVINGS', '{"points": 500}'),
('Expert Budget', 'Vous maîtrisez la gestion budgétaire', '📊', 'BUDGET', '{"level": 5}'),
('Étudiant assidu', 'Vous avez complété 5 cours', '📚', 'EDUCATION', '{"coursesCompleted": 5}'),
('Défi mensuel', 'Vous avez respecté votre budget ce mois', '🏆', 'BUDGET', '{"budgetRespected": true}')
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    icon = VALUES(icon);

-- 6. COURSES
INSERT INTO courses (title, description, content, category, difficulty, duration_minutes, language, is_active, created_at)
VALUES 
('Introduction à la gestion budgétaire', 
 'Apprenez les bases de la gestion budgétaire personnelle',
 'La gestion budgétaire est la base de toute bonne santé financière. Dans ce cours, vous apprendrez à créer un budget mensuel, suivre vos dépenses, et économiser efficacement.',
 'BUDGETING', 'BEGINNER', 30, 'FR', true, NOW()),
('Les bases de l''épargne', 
 'Découvrez comment épargner efficacement',
 'L''épargne est essentielle pour votre sécurité financière. Ce cours vous enseignera pourquoi épargner est important et comment fixer des objectifs réalistes.',
 'SAVINGS', 'BEGINNER', 25, 'FR', true, NOW()),
('Comprendre le crédit et la dette', 
 'Apprenez à gérer le crédit intelligemment',
 'Le crédit peut être un outil utile s''il est bien géré. Dans ce cours, vous découvrirez les types de crédit, comment fonctionnent les intérêts, et comment éviter le surendettement.',
 'CREDIT', 'INTERMEDIATE', 35, 'FR', true, NOW())
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    content = VALUES(content);

-- 7. QUIZZES (pour le premier cours)
-- Note: Les options sont stockées dans une table séparée quiz_options
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 
    'Quelle est la première étape pour créer un budget ?',
    0,
    'La première étape est de lister tous vos revenus mensuels pour savoir combien vous gagnez réellement.',
    (SELECT id FROM courses WHERE title = 'Introduction à la gestion budgétaire' LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM quizzes 
    WHERE question = 'Quelle est la première étape pour créer un budget ?'
    AND course_id = (SELECT id FROM courses WHERE title = 'Introduction à la gestion budgétaire' LIMIT 1)
);

-- Options pour le quiz
SET @quiz_id = (SELECT id FROM quizzes WHERE question = 'Quelle est la première étape pour créer un budget ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz_id, 'Lister vos revenus');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz_id, 'Lister vos dépenses');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz_id, 'Calculer vos économies');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz_id, 'Ouvrir un compte bancaire');

-- 8. NOTIFICATIONS
INSERT INTO notifications (title, message, type, is_read, created_at, scheduled_for, user_id)
VALUES 
('Budget Alimentation', 'Vous avez dépensé 80% de votre budget alimentation ce mois', 'BUDGET_ALERT', false, NOW(), NOW(), 1),
('Conseil du jour', 'Pensez à revoir vos dépenses mensuelles pour optimiser votre épargne', 'EDUCATIONAL_TIP', false, NOW(), NOW(), 1);

