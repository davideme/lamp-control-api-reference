-- Rollback soft delete support

-- Drop index
DROP INDEX IF EXISTS idx_lamps_deleted_at;

-- Drop column
ALTER TABLE lamps
DROP COLUMN IF EXISTS deleted_at;
