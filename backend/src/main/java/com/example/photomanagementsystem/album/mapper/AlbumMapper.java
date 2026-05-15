package com.example.photomanagementsystem.album.mapper;

import com.example.photomanagementsystem.album.entity.Album;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 相册数据访问，对应 pm_album 表。
 */
@Repository
public class AlbumMapper {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Album> albumRowMapper = (resultSet, rowNum) -> {
        Album album = new Album();
        album.setId(resultSet.getLong("id"));
        album.setUserId(resultSet.getLong("user_id"));
        album.setName(resultSet.getString("name"));
        album.setDescription(resultSet.getString("description"));
        album.setCoverPhotoId(resultSet.getObject("cover_photo_id", Long.class));
        album.setDefaultAlbum(resultSet.getBoolean("is_default"));
        album.setDeleted(resultSet.getBoolean("is_deleted"));
        album.setCreateTime(resultSet.getTimestamp("created_at").toLocalDateTime());
        album.setUpdateTime(resultSet.getTimestamp("updated_at").toLocalDateTime());
        return album;
    };

    public AlbumMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Album insert(Album album) {
        String sql = """
                INSERT INTO pm_album
                    (user_id, name, description, cover_photo_id, is_default, is_deleted, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        Long id = jdbcTemplate.queryForObject(sql, Long.class, album.getUserId(), album.getName(),
                album.getDescription(), album.getCoverPhotoId(), album.getDefaultAlbum(), album.getDeleted(),
                album.getCreateTime(), album.getUpdateTime());
        album.setId(id);
        return album;
    }

    public List<Album> selectListByUserId(Long userId) {
        String sql = """
                SELECT id, user_id, name, description, cover_photo_id, is_default, is_deleted, created_at, updated_at
                FROM pm_album
                WHERE user_id = ? AND is_deleted = FALSE
                ORDER BY is_default DESC, created_at DESC, id DESC
                """;
        return jdbcTemplate.query(sql, albumRowMapper, userId);
    }

    public Optional<Album> selectByIdAndUserId(Long id, Long userId) {
        String sql = """
                SELECT id, user_id, name, description, cover_photo_id, is_default, is_deleted, created_at, updated_at
                FROM pm_album
                WHERE id = ? AND user_id = ? AND is_deleted = FALSE
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, albumRowMapper, id, userId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<Album> selectByNameAndUserId(String name, Long userId) {
        String sql = """
                SELECT id, user_id, name, description, cover_photo_id, is_default, is_deleted, created_at, updated_at
                FROM pm_album
                WHERE name = ? AND user_id = ? AND is_deleted = FALSE
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, albumRowMapper, name, userId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Album updateByIdAndUserId(Album album) {
        String sql = """
                UPDATE pm_album
                SET name = ?, description = ?, updated_at = ?
                WHERE id = ? AND user_id = ?
                """;
        jdbcTemplate.update(sql, album.getName(), album.getDescription(), album.getUpdateTime(),
                album.getId(), album.getUserId());
        return album;
    }

    public void deleteByIdAndUserId(Long id, Long userId) {
        String sql = "DELETE FROM pm_album WHERE id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }
}
