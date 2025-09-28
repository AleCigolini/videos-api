ALTER TABLE videos
ADD COLUMN user_id VARCHAR(255) NOT NULL;

ALTER TABLE videos
ALTER COLUMN azure_blob_url DROP NOT NULL,
ALTER COLUMN container_name DROP NOT NULL;

CREATE INDEX idx_videos_user_id ON videos(user_id);