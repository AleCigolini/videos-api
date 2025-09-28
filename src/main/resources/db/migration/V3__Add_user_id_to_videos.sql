-- Add user ownership column to videos
ALTER TABLE videos
    ADD COLUMN user_id VARCHAR(255) NOT NULL;

-- Optional: create index to speed up user-scoped queries
CREATE INDEX IF NOT EXISTS idx_videos_user_id ON videos(user_id);
