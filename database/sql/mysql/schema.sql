-- MySQL Schema for Lamp Control API
-- Version: 1.0.0

-- Enable strict mode and UTF-8 encoding
SET sql_mode = 'STRICT_ALL_TABLES';
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS lamp_control
    CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

USE lamp_control;

-- Lamps table
CREATE TABLE IF NOT EXISTS lamps (
    id CHAR(36) PRIMARY KEY,  -- UUID format
    is_on BOOLEAN NOT NULL DEFAULT FALSE,  -- true = ON, false = OFF
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,  -- For soft deletes
    INDEX idx_is_on (is_on),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add table comment
ALTER TABLE lamps COMMENT 'Stores lamp entities and their current status';

-- Create trigger for UUID generation if not provided
DELIMITER //
CREATE TRIGGER before_insert_lamps
BEFORE INSERT ON lamps
FOR EACH ROW
BEGIN
    IF NEW.id IS NULL THEN
        SET NEW.id = UUID();
    END IF;
END//
DELIMITER ; 
