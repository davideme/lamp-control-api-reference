-- Rollback soft delete support

-- Drop partial indexes
DROP INDEX IF EXISTS idx_lamps_active_is_on;
DROP INDEX IF EXISTS idx_lamps_active_created_at_id;

-- Drop column
ALTER TABLE lamps
DROP COLUMN IF EXISTS deleted_at;
