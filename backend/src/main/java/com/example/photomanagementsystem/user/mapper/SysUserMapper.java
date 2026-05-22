package com.example.photomanagementsystem.user.mapper;

import com.example.photomanagementsystem.user.entity.SysUser;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * User data access for sys_user.
 */
@Repository
public class SysUserMapper {

    private static final String USER_COLUMNS = """
            id, username, password, nickname, email, avatar, role, status, created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SysUser> userRowMapper = (resultSet, rowNum) -> {
        SysUser user = new SysUser();
        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setNickname(resultSet.getString("nickname"));
        user.setEmail(resultSet.getString("email"));
        user.setAvatar(resultSet.getString("avatar"));
        user.setRole(resultSet.getString("role"));
        user.setStatus(resultSet.getObject("status", Integer.class));
        user.setCreateTime(toLocalDateTime(resultSet.getTimestamp("created_at")));
        user.setUpdateTime(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return user;
    };

    public SysUserMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SysUser insert(SysUser user) {
        String sql = """
                INSERT INTO sys_user
                    (username, password, nickname, email, avatar, role, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        Long id = jdbcTemplate.queryForObject(sql, Long.class, user.getUsername(), user.getPassword(),
                user.getNickname(), user.getEmail(), user.getAvatar(), user.getRole(), user.getStatus(),
                user.getCreateTime(), user.getUpdateTime());
        user.setId(id);
        return user;
    }

    public Optional<SysUser> selectById(Long id) {
        String sql = "SELECT %s FROM sys_user WHERE id = ?".formatted(USER_COLUMNS);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<SysUser> selectByUsername(String username) {
        String sql = "SELECT %s FROM sys_user WHERE username = ?".formatted(USER_COLUMNS);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, username));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(1) FROM sys_user WHERE username = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, username);
        return count != null && count > 0;
    }

    public SysUser updateProfile(SysUser user) {
        String sql = """
                UPDATE sys_user
                SET nickname = ?, email = ?, avatar = ?, updated_at = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, user.getNickname(), user.getEmail(), user.getAvatar(),
                user.getUpdateTime(), user.getId());
        return user;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
