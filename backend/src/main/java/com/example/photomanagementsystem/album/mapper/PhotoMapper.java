package com.example.photomanagementsystem.album.mapper;

import com.example.photomanagementsystem.album.entity.Photo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PhotoMapper {

    private static final String PHOTO_COLUMNS = """
            photo.id, photo.filename, photo.original_name, photo.file_size, photo.mime_type,
            photo.width, photo.height, photo.shot_at, photo.latitude, photo.longitude,
            photo.location_name, photo.camera_make, photo.camera_model, photo.is_favorite,
            photo.created_at, photo.updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Photo> photoRowMapper = (resultSet, rowNum) -> {
        Photo photo = new Photo();
        photo.setId(resultSet.getLong("id"));
        photo.setFilename(resultSet.getString("filename"));
        photo.setOriginalName(resultSet.getString("original_name"));
        photo.setFileSize(resultSet.getObject("file_size", Long.class));
        photo.setMimeType(resultSet.getString("mime_type"));
        photo.setWidth(resultSet.getObject("width", Integer.class));
        photo.setHeight(resultSet.getObject("height", Integer.class));
        photo.setShotAt(toLocalDateTime(resultSet.getTimestamp("shot_at")));
        photo.setLatitude(resultSet.getBigDecimal("latitude"));
        photo.setLongitude(resultSet.getBigDecimal("longitude"));
        photo.setLocationName(resultSet.getString("location_name"));
        photo.setCameraMake(resultSet.getString("camera_make"));
        photo.setCameraModel(resultSet.getString("camera_model"));
        photo.setFavorite(resultSet.getBoolean("is_favorite"));
        photo.setCreateTime(toLocalDateTime(resultSet.getTimestamp("created_at")));
        photo.setUpdateTime(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return photo;
    };

    public PhotoMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByIdAndUserId(Long id, Long userId) {
        String sql = "SELECT COUNT(1) FROM pm_photo WHERE id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id, userId);
        return count != null && count > 0;
    }

    public List<Photo> selectListByAlbumIdAndUserId(Long albumId, Long userId) {
        String sql = """
                SELECT %s
                FROM pm_photo photo
                INNER JOIN pm_album_photo relation ON relation.photo_id = photo.id
                WHERE relation.album_id = ? AND photo.user_id = ?
                ORDER BY COALESCE(photo.shot_at, photo.created_at) DESC, relation.created_at DESC, photo.id DESC
                """.formatted(PHOTO_COLUMNS);
        return jdbcTemplate.query(sql, photoRowMapper, albumId, userId);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
