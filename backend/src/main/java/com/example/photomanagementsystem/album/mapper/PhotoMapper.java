package com.example.photomanagementsystem.album.mapper;

import com.example.photomanagementsystem.album.entity.Photo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 照片数据最小访问边界，对应 pm_photo 表。
 */
@Repository
public class PhotoMapper {

    private final JdbcTemplate jdbcTemplate;

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
                SELECT photo.id
                FROM pm_photo photo
                INNER JOIN pm_album_photo relation ON relation.photo_id = photo.id
                WHERE relation.album_id = ? AND photo.user_id = ?
                ORDER BY relation.created_at DESC, photo.id DESC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            Photo photo = new Photo();
            photo.setId(resultSet.getLong("id"));
            return photo;
        }, albumId, userId);
    }
}
