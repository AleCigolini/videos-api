CREATE TABLE videos (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    azure_blob_url TEXT NULL,
    container_name VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_videos_status ON videos(status);
CREATE INDEX idx_videos_uploaded_at ON videos(uploaded_at);
CREATE INDEX idx_videos_stored_file_name ON videos(stored_file_name);
CREATE INDEX idx_videos_original_file_name ON videos(original_file_name);
CREATE INDEX idx_videos_user_id ON videos(user_id);

-- Add constraints
ALTER TABLE videos ADD CONSTRAINT chk_videos_status 
    CHECK (status IN ('UPLOADED', 'PROCESSING', 'PROCESSED', 'FAILED'));

ALTER TABLE videos ADD CONSTRAINT chk_videos_file_size 
    CHECK (file_size > 0);
