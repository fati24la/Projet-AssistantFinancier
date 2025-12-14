-- ============================================
-- DONNÉES DE TEST POUR L'ASSISTANT FINANCIER
-- VERSION SÉCURISÉE (Gère les doublons)
-- ============================================
-- Ce script peut être exécuté plusieurs fois sans erreur
-- Il vérifie l'existence des données avant d'insérer

-- ============================================
-- IMPORTANT : Créer d'abord un utilisateur
-- ============================================
-- Si vous n'avez pas encore d'utilisateur, créez-en un via l'application
-- ou utilisez cette commande (remplacez le hash BCrypt par un vrai hash) :
-- 
-- INSERT INTO users (username, email, password) 
-- VALUES ('testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');
--
-- Le mot de passe ci-dessus est "password123" (hashé avec BCrypt)

-- ============================================
-- 1. USER PROFILE (Profil utilisateur)
-- ============================================
-- Remplacez 1 par l'ID de votre utilisateur
INSERT INTO user_profiles (user_id, language, level, points, level_number, monthly_income, monthly_expenses, total_savings, total_debt, financial_goals, created_at, updated_at)
SELECT 1, 'FR', 'INTERMEDIATE', 350, 4, 5000.00, 3500.00, 15000.00, 5000.00, '{"goals": ["Acheter une voiture", "Construire une maison", "Voyager"]}', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM user_profiles WHERE user_id = 1)
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
-- 2. BUDGETS (Budgets par catégorie)
-- ============================================
-- Supprimer les budgets existants si vous voulez repartir de zéro
-- DELETE FROM budgets WHERE user_id = 1;

INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
SELECT 'Budget Alimentation', 'ALIMENTATION', 1500.00, 1200.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Budget Alimentation' AND category = 'ALIMENTATION');

INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
SELECT 'Budget Transport', 'TRANSPORT', 800.00, 650.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Budget Transport' AND category = 'TRANSPORT');

INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
SELECT 'Budget Santé', 'SANTE', 500.00, 200.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Budget Santé' AND category = 'SANTE');

INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
SELECT 'Budget Éducation', 'EDUCATION', 300.00, 150.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Budget Éducation' AND category = 'EDUCATION');

INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
SELECT 'Budget Loisirs', 'LOISIRS', 400.00, 300.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Budget Loisirs' AND category = 'LOISIRS');

-- ============================================
-- 3. EXPENSES (Dépenses)
-- ============================================
-- Note: Les budget_id seront automatiquement associés si les budgets existent
-- Vous pouvez supprimer les dépenses existantes si nécessaire :
-- DELETE FROM expenses WHERE user_id = 1;

-- Dépenses pour ce mois
INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id, budget_id)
SELECT 'Courses supermarché', 'ALIMENTATION', 450.00, CURDATE() - INTERVAL 5 DAY, 'CARD', NOW(), 1, 
       (SELECT id FROM budgets WHERE user_id = 1 AND category = 'ALIMENTATION' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM expenses WHERE user_id = 1 AND description = 'Courses supermarché' AND date = CURDATE() - INTERVAL 5 DAY);

INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id, budget_id)
SELECT 'Restaurant', 'ALIMENTATION', 150.00, CURDATE() - INTERVAL 2 DAY, 'CARD', NOW(), 1,
       (SELECT id FROM budgets WHERE user_id = 1 AND category = 'ALIMENTATION' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM expenses WHERE user_id = 1 AND description = 'Restaurant' AND date = CURDATE() - INTERVAL 2 DAY);

INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id, budget_id)
SELECT 'Essence', 'TRANSPORT', 300.00, CURDATE() - INTERVAL 3 DAY, 'CARD', NOW(), 1,
       (SELECT id FROM budgets WHERE user_id = 1 AND category = 'TRANSPORT' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM expenses WHERE user_id = 1 AND description = 'Essence' AND date = CURDATE() - INTERVAL 3 DAY);

-- ============================================
-- 4. SAVINGS GOALS (Objectifs d'épargne)
-- ============================================
INSERT INTO savings_goals (name, description, target_amount, current_amount, target_date, completed, created_at, updated_at, user_id)
SELECT 'Achat voiture', 'Économiser pour acheter une voiture d''occasion', 50000.00, 15000.00, DATE_ADD(CURDATE(), INTERVAL 18 MONTH), false, NOW(), NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM savings_goals WHERE user_id = 1 AND name = 'Achat voiture');

INSERT INTO savings_goals (name, description, target_amount, current_amount, target_date, completed, created_at, updated_at, user_id)
SELECT 'Voyage en Europe', 'Budget pour un voyage de 2 semaines en Europe', 20000.00, 5000.00, DATE_ADD(CURDATE(), INTERVAL 7 MONTH), false, NOW(), NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM savings_goals WHERE user_id = 1 AND name = 'Voyage en Europe');

INSERT INTO savings_goals (name, description, target_amount, current_amount, target_date, completed, created_at, updated_at, user_id)
SELECT 'Fonds d''urgence', 'Créer un fonds d''urgence de 3 mois de salaire', 15000.00, 12000.00, DATE_ADD(CURDATE(), INTERVAL 3 MONTH), false, NOW(), NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM savings_goals WHERE user_id = 1 AND name = 'Fonds d''urgence');

-- ============================================
-- 5. BADGES (Badges de gamification)
-- ============================================
INSERT INTO badges (name, description, icon, category, requirement)
SELECT 'Premier pas', 'Vous avez commencé votre parcours financier', '🎯', 'EDUCATION', '{"points": 10}'
WHERE NOT EXISTS (SELECT 1 FROM badges WHERE name = 'Premier pas');

INSERT INTO badges (name, description, icon, category, requirement)
SELECT 'Épargnant', 'Vous avez économisé régulièrement', '💰', 'SAVINGS', '{"points": 500}'
WHERE NOT EXISTS (SELECT 1 FROM badges WHERE name = 'Épargnant');

INSERT INTO badges (name, description, icon, category, requirement)
SELECT 'Expert Budget', 'Vous maîtrisez la gestion budgétaire', '📊', 'BUDGET', '{"level": 5}'
WHERE NOT EXISTS (SELECT 1 FROM badges WHERE name = 'Expert Budget');

INSERT INTO badges (name, description, icon, category, requirement)
SELECT 'Étudiant assidu', 'Vous avez complété 5 cours', '📚', 'EDUCATION', '{"coursesCompleted": 5}'
WHERE NOT EXISTS (SELECT 1 FROM badges WHERE name = 'Étudiant assidu');

INSERT INTO badges (name, description, icon, category, requirement)
SELECT 'Défi mensuel', 'Vous avez respecté votre budget ce mois', '🏆', 'BUDGET', '{"budgetRespected": true}'
WHERE NOT EXISTS (SELECT 1 FROM badges WHERE name = 'Défi mensuel');

-- Associer des badges au profil utilisateur (via user_profile_id)
-- D'abord, obtenir l'ID du profil utilisateur
SET @user_profile_id = (SELECT id FROM user_profiles WHERE user_id = 1 LIMIT 1);

-- Associer les badges au profil
INSERT INTO user_badges (user_profile_id, badge_id)
SELECT @user_profile_id, (SELECT id FROM badges WHERE name = 'Premier pas' LIMIT 1)
WHERE @user_profile_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM user_badges WHERE user_profile_id = @user_profile_id AND badge_id = (SELECT id FROM badges WHERE name = 'Premier pas' LIMIT 1));

INSERT INTO user_badges (user_profile_id, badge_id)
SELECT @user_profile_id, (SELECT id FROM badges WHERE name = 'Épargnant' LIMIT 1)
WHERE @user_profile_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM user_badges WHERE user_profile_id = @user_profile_id AND badge_id = (SELECT id FROM badges WHERE name = 'Épargnant' LIMIT 1));

-- ============================================
-- 6. COURSES (Cours d'éducation financière)
-- ============================================
INSERT INTO courses (title, description, content, category, difficulty, duration_minutes, language, is_active, created_at)
SELECT 'Introduction à la gestion budgétaire', 
       'Apprenez les bases de la gestion budgétaire personnelle',
       'La gestion budgétaire est la base de toute bonne santé financière. Dans ce cours, vous apprendrez à créer un budget mensuel, suivre vos dépenses, et économiser efficacement.',
       'BUDGETING', 'BEGINNER', 30, 'FR', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE title = 'Introduction à la gestion budgétaire');

INSERT INTO courses (title, description, content, category, difficulty, duration_minutes, language, is_active, created_at)
SELECT 'Les bases de l''épargne', 
       'Découvrez comment épargner efficacement',
       'L''épargne est essentielle pour votre sécurité financière. Ce cours vous enseignera pourquoi épargner est important et comment fixer des objectifs réalistes.',
       'SAVINGS', 'BEGINNER', 25, 'FR', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE title = 'Les bases de l''épargne');

INSERT INTO courses (title, description, content, category, difficulty, duration_minutes, language, is_active, created_at)
SELECT 'Comprendre le crédit et la dette', 
       'Apprenez à gérer le crédit intelligemment',
       'Le crédit peut être un outil utile s''il est bien géré. Dans ce cours, vous découvrirez les types de crédit, comment fonctionnent les intérêts, et comment éviter le surendettement.',
       'CREDIT', 'INTERMEDIATE', 35, 'FR', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE title = 'Comprendre le crédit et la dette');

-- ============================================
-- 7. QUIZZES (Questions de quiz)
-- ============================================
-- Note: Les options sont stockées dans une table séparée quiz_options
-- Il faut d'abord insérer le quiz, puis ses options

-- Quiz pour "Introduction à la gestion budgétaire"
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Quelle est la première étape pour créer un budget ?',
       0,
       'La première étape est de lister tous vos revenus mensuels pour savoir combien vous gagnez réellement.',
       (SELECT id FROM courses WHERE title = 'Introduction à la gestion budgétaire' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Quelle est la première étape pour créer un budget ?');

-- Options pour le quiz
SET @quiz_id = (SELECT id FROM quizzes WHERE question = 'Quelle est la première étape pour créer un budget ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz_id, 'Lister vos revenus'
WHERE @quiz_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz_id AND option_text = 'Lister vos revenus');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz_id, 'Lister vos dépenses'
WHERE @quiz_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz_id AND option_text = 'Lister vos dépenses');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz_id, 'Calculer vos économies'
WHERE @quiz_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz_id AND option_text = 'Calculer vos économies');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz_id, 'Ouvrir un compte bancaire'
WHERE @quiz_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz_id AND option_text = 'Ouvrir un compte bancaire');

-- ============================================
-- 8. NOTIFICATIONS (Notifications)
-- ============================================
INSERT INTO notifications (title, message, type, is_read, created_at, scheduled_for, user_id)
SELECT 'Budget Alimentation', 'Vous avez dépensé 80% de votre budget alimentation ce mois', 'BUDGET_ALERT', false, NOW(), NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = 1 AND title = 'Budget Alimentation' AND type = 'BUDGET_ALERT');

INSERT INTO notifications (title, message, type, is_read, created_at, scheduled_for, user_id)
SELECT 'Conseil du jour', 'Pensez à revoir vos dépenses mensuelles pour optimiser votre épargne', 'EDUCATIONAL_TIP', false, NOW(), NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = 1 AND title = 'Conseil du jour' AND type = 'EDUCATIONAL_TIP');

-- ============================================
-- NOTES IMPORTANTES
-- ============================================
-- 1. Remplacez user_id = 1 par l'ID de votre utilisateur réel
-- 2. Ce script peut être exécuté plusieurs fois sans erreur
-- 3. Les données existantes ne seront pas dupliquées
-- 4. Pour supprimer toutes les données de test :
--    DELETE FROM expenses WHERE user_id = 1;
--    DELETE FROM budgets WHERE user_id = 1;
--    DELETE FROM savings_goals WHERE user_id = 1;
--    DELETE FROM user_profiles WHERE user_id = 1;
--    (Ne supprimez pas les cours et badges, ils sont partagés)

