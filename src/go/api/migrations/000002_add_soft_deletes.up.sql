-- Add soft delete support to lamps table
-- Version: 2.0.0

-- Add deleted_at column for soft deletes
ALTER TABLE lamps
ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Add comment for deleted_at column
COMMENT ON COLUMN lamps.deleted_at IS 'Timestamp when the lamp was soft deleted, NULL if active';

-- Add optimized partial indexes for active rows
CREATE INDEX IF NOT EXISTS idx_lamps_active_created_at_id
ON lamps (created_at ASC, id ASC)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_lamps_active_is_on
ON lamps (is_on)
WHERE deleted_at IS NULL;
