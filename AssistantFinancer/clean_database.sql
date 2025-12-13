-- Script de nettoyage COMPLET de la base de données assistant_financier
-- Exécutez ce script dans MySQL pour résoudre le problème de tablespace orphelin
-- ATTENTION: Ce script supprimera TOUTES les données de la base de données !

-- Étape 1: Supprimer la base de données complètement (supprime aussi les tablespaces orphelins)
DROP DATABASE IF EXISTS assistant_financier;

-- Étape 2: Recréer la base de données avec le bon encodage
CREATE DATABASE assistant_financier 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Étape 3: Utiliser la nouvelle base de données
USE assistant_financier;

-- Note: Après l'exécution de ce script, redémarrez l'application Spring Boot
-- Hibernate créera automatiquement les tables avec spring.jpa.hibernate.ddl-auto=create

