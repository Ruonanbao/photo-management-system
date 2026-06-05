package com.example.photomanagementsystem.photo.mapper;

import com.example.photomanagementsystem.photo.entity.AlbumPhoto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Album and photo relation data access.
 */
@Repository
public class PhotoAlbumMapper {

    private final JdbcTemplate jdbcTemplate;

    public PhotoAlbumMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsAlbumByIdAndUserId(Long albumId, Long userId) {
        String sql = "SELECT COUNT(1) FROM pm_album WHERE id = ? AND user_id = ? AND is_deleted = FALSE";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, albumId, userId);
        return count != null && count > 0;
    }

    public boolean existsRelation(Long albumId, Long photoId) {
        String sql = "SELECT COUNT(1) FROM pm_album_photo WHERE album_id = ? AND photo_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, albumId, photoId);
        return count != null && count > 0;
    }

    public void insertRelation(AlbumPhoto albumPhoto) {
        String sql = "INSERT INTO pm_album_photo (album_id, photo_id, created_at) VALUES (?, ?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, albumPhoto.getAlbumId(),
                albumPhoto.getPhotoId(), albumPhoto.getCreateTime());
        albumPhoto.setId(id);
    }

    public void deleteByPhotoIdAndUserId(Long photoId, Long userId) {
        String sql = """
                DELETE FROM pm_album_photo relation
                USING pm_photo photo
                WHERE relation.photo_id = photo.id
                  AND relation.photo_id = ?
                  AND photo.user_id = ?
                """;
        jdbcTemplate.update(sql, photoId, userId);
    }
}
