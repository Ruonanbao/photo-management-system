CREATE TABLE IF NOT EXISTS pm_album_photo (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL REFERENCES pm_album(id),
    photo_id BIGINT NOT NULL REFERENCES pm_photo(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(album_id, photo_id)
);
