package com.example.photomanagementsystem.person.mapper;

import com.example.photomanagementsystem.person.entity.Person;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Person data access for pm_person and pm_face.
 */
@Repository
public class PersonMapper {

    private static final String PERSON_COLUMNS = "id, user_id, name, cover_face_id, created_at, updated_at";

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Person> personRowMapper = (resultSet, rowNum) -> {
        Person person = new Person();
        person.setId(resultSet.getLong("id"));
        person.setUserId(resultSet.getLong("user_id"));
        person.setName(resultSet.getString("name"));
        person.setCoverFaceId(resultSet.getObject("cover_face_id", Long.class));
        person.setCreateTime(toLocalDateTime(resultSet.getTimestamp("created_at")));
        person.setUpdateTime(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return person;
    };

    public PersonMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Person> selectListByUserId(Long userId) {
        String sql = """
                SELECT %s
                FROM pm_person
                WHERE user_id = ?
                ORDER BY updated_at DESC, id DESC
                """.formatted(PERSON_COLUMNS);
        return jdbcTemplate.query(sql, personRowMapper, userId);
    }

    public Optional<Person> selectByIdAndUserId(Long id, Long userId) {
        String sql = "SELECT %s FROM pm_person WHERE id = ? AND user_id = ?".formatted(PERSON_COLUMNS);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, personRowMapper, id, userId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Person updateByIdAndUserId(Person person) {
        String sql = """
                UPDATE pm_person
                SET name = ?, updated_at = ?
                WHERE id = ? AND user_id = ?
                """;
        jdbcTemplate.update(sql, person.getName(), person.getUpdateTime(), person.getId(), person.getUserId());
        return person;
    }

    public void clearFacesByPersonId(Long personId) {
        String sql = "UPDATE pm_face SET person_id = NULL WHERE person_id = ?";
        jdbcTemplate.update(sql, personId);
    }

    public void deleteByIdAndUserId(Long id, Long userId) {
        String sql = "DELETE FROM pm_person WHERE id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }

    public long countPhotosByPersonId(Long personId) {
        String sql = """
                SELECT COUNT(DISTINCT photo_id)
                FROM pm_face
                WHERE person_id = ?
                """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, personId);
        return count == null ? 0L : count;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
