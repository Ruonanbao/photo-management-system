CREATE TABLE IF NOT EXISTS pm_face (
    id BIGSERIAL PRIMARY KEY,
    photo_id BIGINT NOT NULL REFERENCES pm_photo(id),
    feature_vector FLOAT8[],
    bbox_x FLOAT,
    bbox_y FLOAT,
    bbox_width FLOAT,
    bbox_height FLOAT,
    person_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
