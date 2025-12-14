-- ============================================
-- DONNÉES DE TEST POUR L'ASSISTANT FINANCIER
-- ============================================
-- Ce script contient des données de démonstration pour tester toutes les fonctionnalités
-- Exécutez ce script après avoir créé les tables (via JPA ou manuellement)

-- ============================================
-- 0. CRÉER L'UTILISATEUR SI NÉCESSAIRE
-- ============================================
-- IMPORTANT : Créez d'abord un utilisateur via l'application Flutter
-- OU utilisez cette commande (mot de passe: password123) :
-- 
-- INSERT INTO users (username, email, password) 
-- VALUES ('testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');
--
-- Puis vérifiez l'ID de l'utilisateur créé :
-- SELECT id, username FROM users;
--
-- Remplacez "1" dans le script par l'ID réel de votre utilisateur

-- ============================================
-- 1. USER PROFILE (Profil utilisateur)
-- ============================================
-- ⚠️ ATTENTION : Remplacez "1" par l'ID réel de votre utilisateur
-- Vérifiez d'abord que l'utilisateur existe :
-- SELECT * FROM users WHERE id = 1;

INSERT INTO user_profiles (user_id, language, level, points, level_number, monthly_income, monthly_expenses, total_savings, total_debt, financial_goals, created_at, updated_at)
SELECT 1, 'FR', 'INTERMEDIATE', 350, 4, 5000.00, 3500.00, 15000.00, 5000.00, '{"goals": ["Acheter une voiture", "Construire une maison", "Voyager"]}', NOW(), NOW()
WHERE EXISTS (SELECT 1 FROM users WHERE id = 1)
  AND NOT EXISTS (SELECT 1 FROM user_profiles WHERE user_id = 1)
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
-- Supprimer les budgets existants pour cet utilisateur (optionnel)
-- DELETE FROM budgets WHERE user_id = 1;

INSERT INTO budgets (name, category, amount, spent, start_date, end_date, period, created_at, user_id)
SELECT 'Budget Alimentation', 'ALIMENTATION', 1500.00, 1200.00, '2024-01-01', '2024-01-31', 'MONTHLY', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Budget Alimentation')
UNION ALL
SELECT 'Budget Transport', 'TRANSPORT', 800.00, 650.00, '2024-01-01', '2024-01-31', 'MONTHLY', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Budget Transport')
UNION ALL
SELECT 'Budget Santé', 'SANTE', 500.00, 200.00, '2024-01-01', '2024-01-31', 'MONTHLY', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Budget Santé')
UNION ALL
SELECT 'Budget Éducation', 'EDUCATION', 300.00, 150.00, '2024-01-01', '2024-01-31', 'MONTHLY', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Budget Éducation')
UNION ALL
SELECT 'Budget Loisirs', 'LOISIRS', 400.00, 300.00, '2024-01-01', '2024-01-31', 'MONTHLY', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM budgets WHERE user_id = 1 AND name = 'Budget Loisirs');

-- ============================================
-- 3. EXPENSES (Dépenses)
-- ============================================
-- Dépenses pour janvier 2024
INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id, budget_id)
VALUES 
-- Alimentation
('Courses supermarché', 'ALIMENTATION', 450.00, '2024-01-05', 'CARD', NOW(), 1, 1),
('Restaurant', 'ALIMENTATION', 150.00, '2024-01-12', 'CARD', NOW(), 1, 1),
('Marché local', 'ALIMENTATION', 200.00, '2024-01-18', 'CASH', NOW(), 1, 1),
('Épicerie', 'ALIMENTATION', 400.00, '2024-01-25', 'CARD', NOW(), 1, 1),

-- Transport
('Essence', 'TRANSPORT', 300.00, '2024-01-08', 'CARD', NOW(), 1, 2),
('Taxi', 'TRANSPORT', 50.00, '2024-01-15', 'CASH', NOW(), 1, 2),
('Réparation voiture', 'TRANSPORT', 300.00, '2024-01-20', 'CARD', NOW(), 1, 2),

-- Santé
('Consultation médicale', 'SANTE', 150.00, '2024-01-10', 'CARD', NOW(), 1, 3),
('Médicaments', 'SANTE', 50.00, '2024-01-10', 'CARD', NOW(), 1, 3),

-- Éducation
('Livre finance', 'EDUCATION', 80.00, '2024-01-14', 'CARD', NOW(), 1, 4),
('Formation en ligne', 'EDUCATION', 70.00, '2024-01-22', 'CARD', NOW(), 1, 4),

-- Loisirs
('Cinéma', 'LOISIRS', 50.00, '2024-01-06', 'CARD', NOW(), 1, 5),
('Restaurant avec amis', 'LOISIRS', 120.00, '2024-01-13', 'CARD', NOW(), 1, 5),
('Concert', 'LOISIRS', 130.00, '2024-01-27', 'CARD', NOW(), 1, 5);

-- Dépenses pour décembre 2023 (pour les graphiques sur 6 mois)
INSERT INTO expenses (description, category, amount, date, payment_method, created_at, user_id)
VALUES 
('Courses décembre', 'ALIMENTATION', 1400.00, '2023-12-10', 'CARD', NOW(), 1),
('Transport décembre', 'TRANSPORT', 750.00, '2023-12-15', 'CARD', NOW(), 1),
('Loisirs décembre', 'LOISIRS', 350.00, '2023-12-20', 'CARD', NOW(), 1);

-- ============================================
-- 4. SAVINGS GOALS (Objectifs d'épargne)
-- ============================================
INSERT INTO savings_goals (name, description, target_amount, current_amount, target_date, completed, created_at, updated_at, user_id)
VALUES 
('Achat voiture', 'Économiser pour acheter une voiture d''occasion', 50000.00, 15000.00, '2025-06-01', false, NOW(), NOW(), 1),
('Voyage en Europe', 'Budget pour un voyage de 2 semaines en Europe', 20000.00, 5000.00, '2024-08-01', false, NOW(), NOW(), 1),
('Fonds d''urgence', 'Créer un fonds d''urgence de 3 mois de salaire', 15000.00, 12000.00, '2024-04-01', false, NOW(), NOW(), 1),
('Équipement maison', 'Acheter des meubles et équipements', 10000.00, 10000.00, '2024-02-01', true, NOW(), NOW(), 1);

-- ============================================
-- 5. BADGES (Badges de gamification)
-- ============================================
INSERT INTO badges (name, description, icon, category, requirement)
VALUES 
('Premier pas', 'Vous avez commencé votre parcours financier', '🎯', 'EDUCATION', '{"points": 10}'),
('Épargnant', 'Vous avez économisé régulièrement', '💰', 'SAVINGS', '{"points": 500}'),
('Expert Budget', 'Vous maîtrisez la gestion budgétaire', '📊', 'BUDGET', '{"level": 5}'),
('Étudiant assidu', 'Vous avez complété 5 cours', '📚', 'EDUCATION', '{"coursesCompleted": 5}'),
('Défi mensuel', 'Vous avez respecté votre budget ce mois', '🏆', 'BUDGET', '{"budgetRespected": true}');

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
VALUES 
('Introduction à la gestion budgétaire', 
 'Apprenez les bases de la gestion budgétaire personnelle',
 'La gestion budgétaire est la base de toute bonne santé financière. Dans ce cours, vous apprendrez à :
 
1. Créer un budget mensuel
2. Suivre vos dépenses
3. Identifier les postes de dépenses importants
4. Économiser efficacement

Un budget bien géré vous permet de :
- Contrôler vos finances
- Atteindre vos objectifs financiers
- Réduire le stress lié à l''argent
- Préparer l''avenir

Commencez par lister tous vos revenus mensuels, puis vos dépenses fixes (loyer, factures) et variables (alimentation, loisirs). La différence entre revenus et dépenses est votre capacité d''épargne.',
 'BUDGETING', 'BEGINNER', 30, 'FR', true, NOW()),

('Les bases de l''épargne', 
 'Découvrez comment épargner efficacement',
 'L''épargne est essentielle pour votre sécurité financière. Ce cours vous enseignera :

1. Pourquoi épargner est important
2. Comment calculer votre capacité d''épargne
3. Les différents types d''épargne
4. Comment fixer des objectifs d''épargne réalistes

Règle des 50/30/20 :
- 50% pour les besoins essentiels
- 30% pour les envies
- 20% pour l''épargne

Commencez petit : même 100 MAD par mois peuvent faire une différence sur le long terme.',
 'SAVINGS', 'BEGINNER', 25, 'FR', true, NOW()),

('Comprendre le crédit et la dette', 
 'Apprenez à gérer le crédit intelligemment',
 'Le crédit peut être un outil utile s''il est bien géré. Dans ce cours :

1. Types de crédit disponibles
2. Comment fonctionnent les intérêts
3. Calculer le coût réel d''un crédit
4. Éviter le surendettement

Points clés :
- Ne contractez un crédit que si nécessaire
- Comparez toujours les taux d''intérêt
- Lisez attentivement les conditions
- Prévoyez toujours votre capacité de remboursement

Un crédit mal géré peut rapidement devenir un piège financier.',
 'CREDIT', 'INTERMEDIATE', 35, 'FR', true, NOW()),

('L''inclusion bancaire en milieu rural', 
 'Comprendre les services financiers en zone rurale',
 'L''inclusion bancaire est un défi important en milieu rural. Ce cours aborde :

1. Les services bancaires de base
2. L''accès au crédit en zone rurale
3. Les alternatives (microfinance, tontines)
4. Les avantages de l''inclusion financière

En milieu rural, vous pouvez :
- Utiliser les services de mobile money
- Accéder aux points de service bancaires
- Bénéficier de programmes d''inclusion financière
- Participer à des groupes d''épargne communautaires

L''inclusion financière améliore votre qualité de vie et celle de votre famille.',
 'INCLUSION', 'BEGINNER', 40, 'FR', true, NOW()),

('Introduction aux investissements', 
 'Découvrez les bases de l''investissement',
 'Investir permet de faire fructifier votre épargne. Ce cours couvre :

1. Pourquoi investir
2. Types d''investissements (actions, obligations, immobilier)
3. Risque vs rendement
4. Diversification du portefeuille

Rappelez-vous :
- Ne jamais investir plus que ce que vous pouvez perdre
- Diversifiez vos investissements
- Investissez sur le long terme
- Informez-vous avant d''investir

Commencez par des investissements à faible risque avant de vous aventurer.',
 'INVESTMENT', 'ADVANCED', 45, 'FR', true, NOW());

-- ============================================
-- 7. QUIZZES (Questions de quiz)
-- ============================================
-- Note: Les options sont stockées dans une table séparée quiz_options
-- Il faut d'abord insérer le quiz, puis ses options

-- Quiz 1 pour "Introduction à la gestion budgétaire"
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Quelle est la première étape pour créer un budget ?',
       0,
       'La première étape est de lister tous vos revenus mensuels pour savoir combien vous gagnez réellement.',
       (SELECT id FROM courses WHERE title = 'Introduction à la gestion budgétaire' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Quelle est la première étape pour créer un budget ?');

-- Options pour quiz 1
SET @quiz1_id = (SELECT id FROM quizzes WHERE question = 'Quelle est la première étape pour créer un budget ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz1_id, 'Lister vos revenus');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz1_id, 'Lister vos dépenses');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz1_id, 'Calculer vos économies');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz1_id, 'Ouvrir un compte bancaire');

-- Quiz 2
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Quel pourcentage de vos revenus devriez-vous idéalement épargner ?',
       1,
       'La règle des 50/30/20 recommande d''épargner 20% de vos revenus, mais même 10% est un bon début.',
       (SELECT id FROM courses WHERE title = 'Introduction à la gestion budgétaire' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Quel pourcentage de vos revenus devriez-vous idéalement épargner ?');

SET @quiz2_id = (SELECT id FROM quizzes WHERE question = 'Quel pourcentage de vos revenus devriez-vous idéalement épargner ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz2_id, '10%');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz2_id, '20%');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz2_id, '30%');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz2_id, '50%');

-- Quiz 3
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Qu''est-ce qu''une dépense variable ?',
       0,
       'Les dépenses variables changent chaque mois (alimentation, loisirs) contrairement aux dépenses fixes (loyer, factures).',
       (SELECT id FROM courses WHERE title = 'Introduction à la gestion budgétaire' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Qu''est-ce qu''une dépense variable ?');

SET @quiz3_id = (SELECT id FROM quizzes WHERE question = 'Qu''est-ce qu''une dépense variable ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz3_id, 'Une dépense qui change chaque mois');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz3_id, 'Une dépense fixe');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz3_id, 'Une dépense annuelle');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz3_id, 'Une dépense exceptionnelle');

-- Quiz 4 pour "Les bases de l'épargne"
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Pourquoi est-il important d''épargner ?',
       1,
       'L''épargne vous protège en cas d''urgence et vous aide à atteindre vos objectifs financiers.',
       (SELECT id FROM courses WHERE title = 'Les bases de l''épargne' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Pourquoi est-il important d''épargner ?');

SET @quiz4_id = (SELECT id FROM quizzes WHERE question = 'Pourquoi est-il important d''épargner ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz4_id, 'Pour faire des achats impulsifs');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz4_id, 'Pour la sécurité financière et atteindre des objectifs');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz4_id, 'Pour impressionner les autres');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz4_id, 'Ce n''est pas important');

-- Quiz 5 pour "Les bases de l'épargne"
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Quelle est la règle des 50/30/20 ?',
       0,
       'La règle recommande 50% pour les besoins essentiels, 30% pour les envies, et 20% pour l''épargne.',
       (SELECT id FROM courses WHERE title = 'Les bases de l''épargne' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Quelle est la règle des 50/30/20 ?');

SET @quiz5_id = (SELECT id FROM quizzes WHERE question = 'Quelle est la règle des 50/30/20 ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz5_id, '50% besoins, 30% envies, 20% épargne');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz5_id, '50% épargne, 30% besoins, 20% envies');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz5_id, '50% envies, 30% besoins, 20% épargne');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz5_id, '50% besoins, 20% envies, 30% épargne');

-- Quiz 6 pour "Comprendre le crédit et la dette"
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Qu''est-ce qu''un taux d''intérêt ?',
       1,
       'Le taux d''intérêt est le coût de l''emprunt, exprimé en pourcentage du montant emprunté.',
       (SELECT id FROM courses WHERE title = 'Comprendre le crédit et la dette' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Qu''est-ce qu''un taux d''intérêt ?');

SET @quiz6_id = (SELECT id FROM quizzes WHERE question = 'Qu''est-ce qu''un taux d''intérêt ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz6_id, 'Le montant que vous devez rembourser');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz6_id, 'Le coût de l''emprunt exprimé en pourcentage');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz6_id, 'Le montant emprunté');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz6_id, 'La durée du crédit');

-- Quiz 7 pour "Comprendre le crédit et la dette"
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Quand devriez-vous contracter un crédit ?',
       1,
       'Un crédit ne doit être contracté que si c''est nécessaire et si vous êtes sûr de pouvoir rembourser.',
       (SELECT id FROM courses WHERE title = 'Comprendre le crédit et la dette' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Quand devriez-vous contracter un crédit ?');

SET @quiz7_id = (SELECT id FROM quizzes WHERE question = 'Quand devriez-vous contracter un crédit ?' LIMIT 1);
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz7_id, 'Pour tous vos achats');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz7_id, 'Seulement si nécessaire et si vous pouvez rembourser');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz7_id, 'Jamais');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz7_id, 'Pour impressionner');

-- ============================================
-- 8. USER PROGRESS (Progression utilisateur)
-- ============================================
INSERT INTO user_progress (completed, score, started_at, completed_at, user_id, course_id)
VALUES 
(true, 85, '2024-01-01 10:00:00', '2024-01-01 10:30:00', 1, 1), -- Cours 1 complété
(true, 90, '2024-01-02 14:00:00', '2024-01-02 14:25:00', 1, 2), -- Cours 2 complété
(false, 0, '2024-01-10 09:00:00', NULL, 1, 3); -- Cours 3 en cours

-- ============================================
-- 9. NOTIFICATIONS (Notifications)
-- ============================================
INSERT INTO notifications (title, message, type, is_read, created_at, scheduled_for, user_id)
VALUES 
('Budget Alimentation', 'Vous avez dépensé 80% de votre budget alimentation ce mois', 'BUDGET_ALERT', false, NOW(), NOW(), 1),
('Objectif atteint !', 'Félicitations ! Vous avez atteint votre objectif "Équipement maison"', 'GOAL_REACHED', false, NOW(), NOW(), 1),
('Conseil du jour', 'Pensez à revoir vos dépenses mensuelles pour optimiser votre épargne', 'EDUCATIONAL_TIP', false, NOW(), NOW(), 1),
('Rappel budget', 'N''oubliez pas de mettre à jour vos dépenses de la semaine', 'REMINDER', false, NOW(), NOW(), 1),
('Nouveau cours disponible', 'Un nouveau cours sur l''investissement est maintenant disponible', 'EDUCATIONAL_TIP', true, '2024-01-15 08:00:00', '2024-01-15 08:00:00', 1);

-- ============================================
-- NOTES IMPORTANTES
-- ============================================
-- 1. Assurez-vous que l'utilisateur avec id=1 existe dans la table users
-- 2. Les dates sont au format MySQL (YYYY-MM-DD)
-- 3. Les montants sont en MAD (Dirhams marocains)
-- 4. Certaines données référencent des budgets (budget_id), ajustez selon vos IDs
-- 5. Pour tester, vous pouvez modifier les user_id si nécessaire

-- ============================================
-- REQUÊTES UTILES POUR VÉRIFIER
-- ============================================
-- Voir tous les budgets d'un utilisateur :
-- SELECT * FROM budgets WHERE user_id = 1;

-- Voir toutes les dépenses d'un utilisateur :
-- SELECT * FROM expenses WHERE user_id = 1 ORDER BY date DESC;

-- Voir les objectifs d'épargne :
-- SELECT * FROM savings_goals WHERE user_id = 1;

-- Voir les cours disponibles :
-- SELECT * FROM courses WHERE is_active = true;

-- Voir la progression de l'utilisateur :
-- SELECT * FROM user_progress WHERE user_id = 1;

-- Voir les notifications non lues :
-- SELECT * FROM notifications WHERE user_id = 1 AND is_read = false;

