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

    public int countByAlbumId(Long albumId) {
        String sql = "SELECT COUNT(1) FROM pm_album_photo WHERE album_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, albumId);
        return count == null ? 0 : count;
    }

    public boolean existsByAlbumIdAndPhotoId(Long albumId, Long photoId) {
        String sql = "SELECT COUNT(1) FROM pm_album_photo WHERE album_id = ? AND photo_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, albumId, photoId);
        return count != null && count > 0;
    }

    public void insert(AlbumPhoto albumPhoto) {
        String sql = "INSERT INTO pm_album_photo (album_id, photo_id, created_at) VALUES (?, ?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, albumPhoto.getAlbumId(),
                albumPhoto.getPhotoId(), albumPhoto.getCreateTime());
        albumPhoto.setId(id);
    }

    public void deleteByAlbumIdAndPhotoId(Long albumId, Long photoId) {
        String sql = "DELETE FROM pm_album_photo WHERE album_id = ? AND photo_id = ?";
        jdbcTemplate.update(sql, albumId, photoId);
    }

    public void deleteByAlbumId(Long albumId) {
        String sql = "DELETE FROM pm_album_photo WHERE album_id = ?";
        jdbcTemplate.update(sql, albumId);
    }
}
