# Correction : Structure des Quiz dans la Base de Données

## Problème

L'erreur `#1054 - Unknown column 'options' in 'field list'` se produisait car les scripts SQL tentaient d'insérer les options directement dans la colonne `options` de la table `quizzes`, alors que cette colonne n'existe pas.

## Explication

Dans le modèle JPA `Quiz.java`, les options sont stockées dans une **table séparée** `quiz_options` grâce à l'annotation `@ElementCollection` :

```java
@ElementCollection
@CollectionTable(name = "quiz_options", joinColumns = @JoinColumn(name = "quiz_id"))
@Column(name = "option_text")
private List<String> options;
```

Cela signifie que :
- La table `quizzes` contient : `id`, `question`, `correct_answer_index`, `explanation`, `course_id`
- La table `quiz_options` contient : `quiz_id`, `option_text` (une ligne par option)

## Solution

Tous les scripts SQL ont été corrigés pour :
1. **D'abord insérer le quiz** dans la table `quizzes` (sans la colonne `options`)
2. **Ensuite insérer chaque option** dans la table `quiz_options` avec le `quiz_id` correspondant

### Exemple de syntaxe corrigée :

```sql
-- 1. Insérer le quiz
INSERT INTO quizzes (question, correct_answer_index, explanation, course_id)
SELECT 'Quelle est la première étape pour créer un budget ?',
       0,
       'La première étape est de lister tous vos revenus mensuels...',
       (SELECT id FROM courses WHERE title = 'Introduction à la gestion budgétaire' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM quizzes WHERE question = 'Quelle est la première étape pour créer un budget ?');

-- 2. Récupérer l'ID du quiz inséré
SET @quiz_id = (SELECT id FROM quizzes WHERE question = 'Quelle est la première étape pour créer un budget ?' LIMIT 1);

-- 3. Insérer les options
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz_id, 'Lister vos revenus');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz_id, 'Lister vos dépenses');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz_id, 'Calculer vos économies');
INSERT INTO quiz_options (quiz_id, option_text) VALUES (@quiz_id, 'Ouvrir un compte bancaire');
```

## Fichiers corrigés

Les scripts suivants ont été mis à jour :
- ✅ `test_data.sql`
- ✅ `test_data_safe.sql`
- ✅ `test_data_complete.sql`
- ✅ `test_data_clean.sql`

## Prochaines étapes

Vous pouvez maintenant exécuter n'importe lequel de ces scripts SQL sans erreur. Les quiz seront correctement insérés avec leurs options dans la table séparée `quiz_options`.

## Vérification

Pour vérifier que les quiz ont été correctement insérés :

```sql
-- Voir tous les quiz
SELECT * FROM quizzes;

-- Voir les options d'un quiz spécifique
SELECT q.question, qo.option_text, q.correct_answer_index
FROM quizzes q
LEFT JOIN quiz_options qo ON q.id = qo.quiz_id
WHERE q.id = 1
ORDER BY qo.option_text;
```

