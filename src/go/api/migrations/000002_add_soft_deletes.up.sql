-- Add soft delete support to lamps table
-- Version: 2.0.0

-- Add deleted_at column for soft deletes
ALTER TABLE lamps
ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Add index on deleted_at for query performance
CREATE INDEX idx_lamps_deleted_at ON lamps (deleted_at);

-- Add comment for deleted_at column
COMMENT ON COLUMN lamps.deleted_at IS 'Timestamp when the lamp was soft deleted, NULL if active';
