ALTER TABLE post
    ADD COLUMN views INT NOT NULL DEFAULT 0;

CREATE TABLE post_view (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    viewer_key VARCHAR(96) NOT NULL,
    view_bucket BIGINT NOT NULL,
    viewed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration_seconds INT NOT NULL,
    UNIQUE (post_id, viewer_key, view_bucket)
);

CREATE INDEX idx_post_view_post_viewer_viewed_at ON post_view(post_id, viewer_key, viewed_at DESC);
