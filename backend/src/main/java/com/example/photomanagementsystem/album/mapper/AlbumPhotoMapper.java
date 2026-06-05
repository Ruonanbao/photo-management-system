package com.example.photomanagementsystem.album.mapper;

import com.example.photomanagementsystem.album.entity.AlbumPhoto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 相册照片关系数据访问，对应 pm_album_photo 表。
 */
@Repository
public class AlbumPhotoMapper {

    private final JdbcTemplate jdbcTemplate;

    public AlbumPhotoMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countByAlbumIdAndUserId(Long albumId, Long userId) {
        String sql = """
                SELECT COUNT(1)
                FROM pm_album_photo relation
                INNER JOIN pm_album album ON album.id = relation.album_id
                WHERE relation.album_id = ? AND album.user_id = ? AND album.is_deleted = FALSE
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, albumId, userId);
        return count == null ? 0 : count;
    }

    public boolean existsByAlbumIdAndPhotoIdAndUserId(Long albumId, Long photoId, Long userId) {
        String sql = """
                SELECT COUNT(1)
                FROM pm_album_photo relation
                INNER JOIN pm_album album ON album.id = relation.album_id
                INNER JOIN pm_photo photo ON photo.id = relation.photo_id
                WHERE relation.album_id = ?
                  AND relation.photo_id = ?
                  AND album.user_id = ?
                  AND photo.user_id = ?
                  AND album.is_deleted = FALSE
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, albumId, photoId, userId, userId);
        return count != null && count > 0;
    }

    public void insert(AlbumPhoto albumPhoto) {
        String sql = "INSERT INTO pm_album_photo (album_id, photo_id, created_at) VALUES (?, ?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, albumPhoto.getAlbumId(),
                albumPhoto.getPhotoId(), albumPhoto.getCreateTime());
        albumPhoto.setId(id);
    }

    public void deleteByAlbumIdAndPhotoIdAndUserId(Long albumId, Long photoId, Long userId) {
        String sql = """
                DELETE FROM pm_album_photo relation
                USING pm_album album, pm_photo photo
                WHERE relation.album_id = album.id
                  AND relation.photo_id = photo.id
                  AND relation.album_id = ?
                  AND relation.photo_id = ?
                  AND album.user_id = ?
                  AND photo.user_id = ?
                """;
        jdbcTemplate.update(sql, albumId, photoId, userId, userId);
    }

    public void deleteByAlbumIdAndUserId(Long albumId, Long userId) {
        String sql = """
                DELETE FROM pm_album_photo relation
                USING pm_album album
                WHERE relation.album_id = album.id
                  AND relation.album_id = ?
                  AND album.user_id = ?
                """;
        jdbcTemplate.update(sql, albumId, userId);
    }
}
