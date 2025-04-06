-- PostgreSQL Schema for Lamp Control API
-- Version: 1.0.0

-- Create database if connecting as superuser
-- DO $$ 
-- BEGIN
--     CREATE DATABASE lamp_control;
-- EXCEPTION
--     WHEN duplicate_database THEN
--         NULL;
-- END $$;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Lamps table
CREATE TABLE IF NOT EXISTS lamps (
    id UUID PRIMARY KEY DEFAULT UUID_GENERATE_V4(),
    is_on BOOLEAN NOT NULL DEFAULT FALSE,  -- true = ON, false = OFF
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Add table comment
COMMENT ON TABLE lamps IS 'Stores lamp entities and their current status';
COMMENT ON COLUMN lamps.id IS 'Unique identifier for the lamp';
COMMENT ON COLUMN lamps.is_on IS 'Current status of the lamp (true = ON, false = OFF)';
COMMENT ON COLUMN lamps.created_at IS 'Timestamp when the lamp was created';
COMMENT ON COLUMN lamps.updated_at IS 'Timestamp when the lamp was last updated';
COMMENT ON COLUMN lamps.deleted_at IS 'Timestamp when the lamp was soft deleted, NULL if active';

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_lamps_is_on ON lamps (is_on);
CREATE INDEX IF NOT EXISTS idx_lamps_created_at ON lamps (created_at);
CREATE INDEX IF NOT EXISTS idx_lamps_deleted_at ON lamps (deleted_at);

-- Create updated_at trigger
CREATE OR REPLACE FUNCTION UPDATE_UPDATED_AT_COLUMN()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER update_lamps_updated_at
BEFORE UPDATE ON lamps
FOR EACH ROW
EXECUTE FUNCTION UPDATE_UPDATED_AT_COLUMN();
