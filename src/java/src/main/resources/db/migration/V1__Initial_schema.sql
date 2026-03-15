-- PostgreSQL Schema for Lamp Control API
-- Version: 1.0.0

-- Lamps table
CREATE TABLE IF NOT EXISTS lamps (
    id UUID PRIMARY KEY DEFAULT GEN_RANDOM_UUID(),
    is_on BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add table comments
COMMENT ON TABLE lamps IS 'Stores lamp entities and their current status';
COMMENT ON COLUMN lamps.id IS 'Unique identifier for the lamp';
COMMENT ON COLUMN lamps.is_on IS 'Current status of the lamp (true = ON, false = OFF)';
COMMENT ON COLUMN lamps.created_at IS 'Timestamp when the lamp was created';
COMMENT ON COLUMN lamps.updated_at IS 'Timestamp when the lamp was last updated (managed by application layer)';
