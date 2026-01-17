-- Rollback initial schema

-- Drop indexes
DROP INDEX IF EXISTS idx_lamps_created_at;
DROP INDEX IF EXISTS idx_lamps_is_on;

-- Drop table
DROP TABLE IF EXISTS lamps;

-- Drop extension (optional - might be used by other tables)
-- DROP EXTENSION IF EXISTS "uuid-ossp";
