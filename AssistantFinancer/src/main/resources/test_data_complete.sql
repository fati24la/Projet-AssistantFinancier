-- ============================================
-- SCRIPT COMPLET D'INITIALISATION DES DONNÉES
-- ============================================
-- Ce script crée d'abord un utilisateur si nécessaire, puis insère toutes les données
-- Peut être exécuté plusieurs fois sans erreur

-- ============================================
-- ÉTAPE 1 : CRÉER UN UTILISATEUR SI NÉCESSAIRE
-- ============================================
-- Le mot de passe "password123" hashé avec BCrypt
-- Créer l'utilisateur s'il n'existe pas déjà
INSERT INTO users (username, email, password)
SELECT 'testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser');

-- Obtenir l'ID de l'utilisateur créé ou existant
-- Si aucun utilisateur n'existe, cette variable sera NULL
-- Dans ce cas, vous DEVEZ créer l'utilisateur d'abord via l'application ou SQL
SET @user_id = (SELECT id FROM users WHERE username = 'testuser' LIMIT 1);

-- Si @user_id est NULL, le script échouera
-- Solution : Créez l'utilisateur d'abord, puis réexécutez ce script

-- ============================================
-- ÉTAPE 2 : CRÉER LE PROFIL UTILISATEUR
-- ============================================
INSERT INTO user_profiles (user_id, language, level, points, level_number, monthly_income, monthly_expenses, total_savings, total_debt, financial_goals, created_at, updated_at)
SELECT @user_id, 'FR', 'INTERMEDIATE', 350, 4, 5000.00, 3500.00, 15000.00, 5000.00, '{"goals": ["Acheter une voiture", "Construire une maison", "Voyager"]}', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM user_profiles WHERE user_id = @user_id)
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
-- ÉTAPE 3 : CRÉER LES BUDGETS
-- ============================================
INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
SELECT 'Budget Alimentation', 'ALIMENTATION', 1500.00, 1200.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), @user_id
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = @user_id AND name = 'Budget Alimentation');

INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
SELECT 'Budget Transport', 'TRANSPORT', 800.00, 650.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), @user_id
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = @user_id AND name = 'Budget Transport');

INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
SELECT 'Budget Santé', 'SANTE', 500.00, 200.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), @user_id
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = @user_id AND name = 'Budget Santé');

INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
SELECT 'Budget Éducation', 'EDUCATION', 300.00, 150.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), @user_id
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = @user_id AND name = 'Budget Éducation');

INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
SELECT 'Budget Loisirs', 'LOISIRS', 400.00, 300.00, CURDATE(), LAST_DAY(CURDATE()), 'MONTHLY', NOW(), @user_id
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = @user_id AND name = 'Budget Loisirs');

-- ============================================
-- ÉTAPE 4 : CRÉER LES DÉPENSES
-- ============================================
INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id, budget_id)
SELECT 'Courses supermarché', 'ALIMENTATION', 450.00, CURDATE() - INTERVAL 5 DAY, 'CARD', NOW(), @user_id,
       (SELECT id FROM budgets WHERE user_id = @user_id AND category = 'ALIMENTATION' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM expenses WHERE user_id = @user_id AND description = 'Courses supermarché' AND date = CURDATE() - INTERVAL 5 DAY);

INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id, budget_id)
SELECT 'Restaurant', 'ALIMENTATION', 150.00, CURDATE() - INTERVAL 2 DAY, 'CARD', NOW(), @user_id,
       (SELECT id FROM budgets WHERE user_id = @user_id AND category = 'ALIMENTATION' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM expenses WHERE user_id = @user_id AND description = 'Restaurant' AND date = CURDATE() - INTERVAL 2 DAY);

INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id, budget_id)
SELECT 'Essence', 'TRANSPORT', 300.00, CURDATE() - INTERVAL 3 DAY, 'CARD', NOW(), @user_id,
       (SELECT id FROM budgets WHERE user_id = @user_id AND category = 'TRANSPORT' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM expenses WHERE user_id = @user_id AND description = 'Essence' AND date = CURDATE() - INTERVAL 3 DAY);

INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id, budget_id)
SELECT 'Consultation médicale', 'SANTE', 150.00, CURDATE() - INTERVAL 1 DAY, 'CARD', NOW(), @user_id,
       (SELECT id FROM budgets WHERE user_id = @user_id AND category = 'SANTE' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM expenses WHERE user_id = @user_id AND description = 'Consultation médicale' AND date = CURDATE() - INTERVAL 1 DAY);

INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id, budget_id)
SELECT 'Cinéma', 'LOISIRS', 50.00, CURDATE(), 'CARD', NOW(), @user_id,
       (SELECT id FROM budgets WHERE user_id = @user_id AND category = 'LOISIRS' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM expenses WHERE user_id = @user_id AND description = 'Cinéma' AND date = CURDATE());

-- ============================================
-- ÉTAPE 5 : CRÉER LES OBJECTIFS D'ÉPARGNE
-- ============================================
INSERT INTO savings_goals (name, description, target_amount, current_amount, target_date, completed, created_at, updated_at, user_id)
SELECT 'Achat voiture', 'Économiser pour acheter une voiture d''occasion', 50000.00, 15000.00, DATE_ADD(CURDATE(), INTERVAL 18 MONTH), false, NOW(), NOW(), @user_id
WHERE NOT EXISTS (SELECT 1 FROM savings_goals WHERE user_id = @user_id AND name = 'Achat voiture');

INSERT INTO savings_goals (name, description, target_amount, current_amount, target_date, completed, created_at, updated_at, user_id)
SELECT 'Voyage en Europe', 'Budget pour un voyage de 2 semaines en Europe', 20000.00, 5000.00, DATE_ADD(CURDATE(), INTERVAL 7 MONTH), false, NOW(), NOW(), @user_id
WHERE NOT EXISTS (SELECT 1 FROM savings_goals WHERE user_id = @user_id AND name = 'Voyage en Europe');

INSERT INTO savings_goals (name, description, target_amount, current_amount, target_date, completed, created_at, updated_at, user_id)
SELECT 'Fonds d''urgence', 'Créer un fonds d''urgence de 3 mois de salaire', 15000.00, 12000.00, DATE_ADD(CURDATE(), INTERVAL 3 MONTH), false, NOW(), NOW(), @user_id
WHERE NOT EXISTS (SELECT 1 FROM savings_goals WHERE user_id = @user_id AND name = 'Fonds d''urgence');

-- ============================================
-- ÉTAPE 6 : CRÉER LES BADGES
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
SET @user_profile_id = (SELECT id FROM user_profiles WHERE user_id = @user_id LIMIT 1);

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
-- ÉTAPE 7 : CRÉER LES COURS
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

INSERT INTO courses (title, description, content, category, difficulty, duration_minutes, language, is_active, created_at)
SELECT 'L''inclusion bancaire en milieu rural', 
       'Comprendre les services financiers en zone rurale',
       'L''inclusion bancaire est un défi important en milieu rural. Ce cours aborde les services bancaires de base, l''accès au crédit, et les alternatives comme la microfinance.',
       'INCLUSION', 'BEGINNER', 40, 'FR', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE title = 'L''inclusion bancaire en milieu rural');

-- ============================================
-- ÉTAPE 8 : CRÉER LES QUIZZES
-- ============================================
-- Note: Les options sont stockées dans une table séparée quiz_options
-- Il faut d'abord insérer le quiz, puis ses options

-- Quiz 1 pour "Introduction à la gestion budgétaire"
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Quelle est la première étape pour créer un budget ?',
       0,
       'La première étape est de lister tous vos revenus mensuels pour savoir combien vous gagnez réellement.',
       (SELECT id FROM courses WHERE title = 'Introduction à la gestion budgétaire' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Quelle est la première étape pour créer un budget ?' 
                  AND course_id = (SELECT id FROM courses WHERE title = 'Introduction à la gestion budgétaire' LIMIT 1));

-- Options pour le quiz 1
SET @quiz1_id = (SELECT id FROM quizzes WHERE question = 'Quelle est la première étape pour créer un budget ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz1_id, 'Lister vos revenus'
WHERE @quiz1_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz1_id AND option_text = 'Lister vos revenus');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz1_id, 'Lister vos dépenses'
WHERE @quiz1_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz1_id AND option_text = 'Lister vos dépenses');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz1_id, 'Calculer vos économies'
WHERE @quiz1_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz1_id AND option_text = 'Calculer vos économies');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz1_id, 'Ouvrir un compte bancaire'
WHERE @quiz1_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz1_id AND option_text = 'Ouvrir un compte bancaire');

-- Quiz 2 pour "Introduction à la gestion budgétaire"
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Quel pourcentage de vos revenus devriez-vous idéalement épargner ?',
       1,
       'La règle des 50/30/20 recommande d''épargner 20% de vos revenus, mais même 10% est un bon début.',
       (SELECT id FROM courses WHERE title = 'Introduction à la gestion budgétaire' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Quel pourcentage de vos revenus devriez-vous idéalement épargner ?'
                  AND course_id = (SELECT id FROM courses WHERE title = 'Introduction à la gestion budgétaire' LIMIT 1));

-- Options pour le quiz 2
SET @quiz2_id = (SELECT id FROM quizzes WHERE question = 'Quel pourcentage de vos revenus devriez-vous idéalement épargner ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz2_id, '10%'
WHERE @quiz2_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz2_id AND option_text = '10%');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz2_id, '20%'
WHERE @quiz2_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz2_id AND option_text = '20%');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz2_id, '30%'
WHERE @quiz2_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz2_id AND option_text = '30%');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz2_id, '50%'
WHERE @quiz2_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz2_id AND option_text = '50%');

-- Quiz 3 pour "Les bases de l'épargne"
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Pourquoi est-il important d''épargner ?',
       1,
       'L''épargne vous protège en cas d''urgence et vous aide à atteindre vos objectifs financiers.',
       (SELECT id FROM courses WHERE title = 'Les bases de l''épargne' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Pourquoi est-il important d''épargner ?'
                  AND course_id = (SELECT id FROM courses WHERE title = 'Les bases de l''épargne' LIMIT 1));

-- Options pour le quiz 3
SET @quiz3_id = (SELECT id FROM quizzes WHERE question = 'Pourquoi est-il important d''épargner ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz3_id, 'Pour faire des achats impulsifs'
WHERE @quiz3_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz3_id AND option_text = 'Pour faire des achats impulsifs');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz3_id, 'Pour la sécurité financière et atteindre des objectifs'
WHERE @quiz3_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz3_id AND option_text = 'Pour la sécurité financière et atteindre des objectifs');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz3_id, 'Pour impressionner les autres'
WHERE @quiz3_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz3_id AND option_text = 'Pour impressionner les autres');
INSERT INTO quiz_options (quiz_id, option_text)
SELECT @quiz3_id, 'Ce n''est pas important'
WHERE @quiz3_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quiz_options WHERE quiz_id = @quiz3_id AND option_text = 'Ce n''est pas important');

-- ============================================
-- ÉTAPE 9 : CRÉER LES NOTIFICATIONS
-- ============================================
INSERT INTO notifications (title, message, type, is_read, created_at, scheduled_for, user_id)
SELECT 'Budget Alimentation', 'Vous avez dépensé 80% de votre budget alimentation ce mois', 'BUDGET_ALERT', false, NOW(), NOW(), @user_id
WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = @user_id AND title = 'Budget Alimentation' AND type = 'BUDGET_ALERT');

INSERT INTO notifications (title, message, type, is_read, created_at, scheduled_for, user_id)
SELECT 'Conseil du jour', 'Pensez à revoir vos dépenses mensuelles pour optimiser votre épargne', 'EDUCATIONAL_TIP', false, NOW(), NOW(), @user_id
WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE user_id = @user_id AND title = 'Conseil du jour' AND type = 'EDUCATIONAL_TIP');

-- ============================================
-- MESSAGE DE CONFIRMATION
-- ============================================
SELECT CONCAT('✅ Données insérées avec succès pour l''utilisateur ID: ', @user_id, ' (username: testuser)') AS message;

