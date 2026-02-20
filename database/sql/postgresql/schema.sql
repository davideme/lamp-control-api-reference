-- PostgreSQL Schema for Lamp Control API
-- Version: 2.0.0
-- Target: PostgreSQL 18+
--
-- Changes from v1.0.0:
--   - Removed uuid-ossp extension; gen_random_uuid() is built-in since PostgreSQL 13
--   - Replaced three single-column full indexes with two optimized partial indexes
--     matching actual query patterns (WHERE deleted_at IS NULL ORDER BY created_at, id)
--   - Updated object formatting to match SQLFluff conventions
--
-- PostgreSQL 18 note: uuidv7() is available natively for time-ordered UUIDs,
-- which improve B-tree index locality at scale. Adopting it requires application-layer
-- changes in all language implementations to generate UUIDv7 instead of UUIDv4.

-- Create database if connecting as superuser
-- DO $$
-- BEGIN
--     CREATE DATABASE lamp_control;
-- EXCEPTION
--     WHEN duplicate_database THEN
--         NULL;
-- END $$;

-- Lamps table
-- gen_random_uuid() generates a UUIDv4; no extension required on PostgreSQL 13+.
CREATE TABLE IF NOT EXISTS lamps (
    id          UUID                     PRIMARY KEY DEFAULT GEN_RANDOM_UUID(),
    is_on       BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP WITH TIME ZONE
);

-- Table and column comments
COMMENT ON TABLE lamps IS 'Stores lamp entities and their current status';
COMMENT ON COLUMN lamps.id IS 'Unique identifier for the lamp';
COMMENT ON COLUMN lamps.is_on IS 'Current status of the lamp (true = ON, false = OFF)';
COMMENT ON COLUMN lamps.created_at IS 'Timestamp when the lamp was created';
COMMENT ON COLUMN lamps.updated_at IS 'Last update timestamp (managed by trigger)';
COMMENT ON COLUMN lamps.deleted_at IS 'Soft-delete timestamp; NULL means active';

-- Indexes
--
-- Covers the dominant list query and pagination ordering:
--   WHERE deleted_at IS NULL ORDER BY created_at ASC, id ASC
-- Also serves COUNT(*) WHERE deleted_at IS NULL via index-only scan.
-- Partial index stores only active (non-deleted) rows, reducing size and
-- improving cache locality compared to a full index.
CREATE INDEX IF NOT EXISTS idx_lamps_active_created_at_id
ON lamps (created_at ASC, id ASC)
WHERE deleted_at IS NULL;

-- Covers status filter queries among active lamps:
--   WHERE is_on = $1 AND deleted_at IS NULL
-- Partial index on active rows only improves selectivity vs. a full boolean index.
CREATE INDEX IF NOT EXISTS idx_lamps_active_is_on
ON lamps (is_on)
WHERE deleted_at IS NULL;

-- Note: a dedicated index on deleted_at is intentionally omitted.
-- The pattern WHERE id = $1 AND deleted_at IS NULL is served by the PK index
-- (single-row lookup, then a NULL check on that one row — effectively free).
-- The pattern WHERE deleted_at IS NULL alone is covered by the partial indexes above.

-- Trigger: automatically maintain updated_at on every UPDATE.
-- CURRENT_TIMESTAMP returns transaction start time, ensuring consistent
-- timestamps for all rows modified within the same transaction.
CREATE OR REPLACE FUNCTION UPDATE_UPDATED_AT_COLUMN()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_lamps_updated_at
BEFORE UPDATE ON lamps
FOR EACH ROW
EXECUTE FUNCTION UPDATE_UPDATED_AT_COLUMN();
