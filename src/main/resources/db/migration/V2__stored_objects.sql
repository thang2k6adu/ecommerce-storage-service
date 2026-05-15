CREATE TABLE stored_objects (
    id UUID PRIMARY KEY,
    object_key VARCHAR(768) NOT NULL UNIQUE,
    url VARCHAR(2048) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    original_filename VARCHAR(512),
    content_type VARCHAR(256),
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stored_objects_created_at ON stored_objects (created_at);
