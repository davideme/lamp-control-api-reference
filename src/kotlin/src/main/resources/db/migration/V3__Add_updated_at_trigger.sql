-- Add automatic updated_at trigger
-- Version: 3.0.0

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger on lamps table
CREATE TRIGGER update_lamps_updated_at
BEFORE UPDATE ON lamps
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Update comment for updated_at column
COMMENT ON COLUMN lamps.updated_at IS 'Timestamp when the lamp was last updated (automatically managed by database trigger)';
