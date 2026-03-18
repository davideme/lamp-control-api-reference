-- Rollback updated_at trigger

-- Drop trigger
DROP TRIGGER IF EXISTS update_lamps_updated_at ON lamps;

-- Drop function
DROP FUNCTION IF EXISTS update_updated_at_column();

-- Restore original comment
COMMENT ON COLUMN lamps.updated_at IS 'Timestamp when the lamp was last updated';
