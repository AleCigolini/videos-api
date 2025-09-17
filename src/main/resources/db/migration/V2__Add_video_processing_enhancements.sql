-- Add additional indexes for better performance on video queries
CREATE INDEX IF NOT EXISTS idx_videos_processed_at ON videos(processed_at);
CREATE INDEX IF NOT EXISTS idx_videos_container_name ON videos(container_name);

-- Add trigger to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_videos_updated_at 
    BEFORE UPDATE ON videos 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Add constraint to ensure processed_at is set when status is PROCESSED
ALTER TABLE videos ADD CONSTRAINT chk_processed_at_when_processed 
    CHECK (
        (status = 'PROCESSED' AND processed_at IS NOT NULL) OR 
        (status != 'PROCESSED')
    );
