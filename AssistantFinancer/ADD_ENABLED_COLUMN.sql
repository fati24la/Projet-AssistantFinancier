-- Script pour ajouter la colonne 'enabled' à la table users
-- Exécutez ce script dans votre base de données MySQL

ALTER TABLE users 
ADD COLUMN enabled BOOLEAN DEFAULT TRUE;

-- Mettre à jour tous les utilisateurs existants pour qu'ils soient activés par défaut
UPDATE users SET enabled = TRUE WHERE enabled IS NULL;

