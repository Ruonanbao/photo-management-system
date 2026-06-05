package com.example.photomanagementsystem.person.mapper;

import com.example.photomanagementsystem.person.vo.PersonPhotoVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Person photo data access for pm_face and pm_photo.
 */
@Repository
public class PersonPhotoMapper {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PersonPhotoVO> photoRowMapper = (resultSet, rowNum) -> {
        PersonPhotoVO photoVO = new PersonPhotoVO();
        photoVO.setId(resultSet.getLong("id"));
        photoVO.setFilename(resultSet.getString("filename"));
        photoVO.setOriginalName(resultSet.getString("original_name"));
        photoVO.setFileSize(resultSet.getObject("file_size", Long.class));
        photoVO.setMimeType(resultSet.getString("mime_type"));
        photoVO.setWidth(resultSet.getObject("width", Integer.class));
        photoVO.setHeight(resultSet.getObject("height", Integer.class));
        photoVO.setShotAt(toLocalDateTime(resultSet.getTimestamp("shot_at")));
        photoVO.setLatitude(resultSet.getBigDecimal("latitude"));
        photoVO.setLongitude(resultSet.getBigDecimal("longitude"));
        photoVO.setLocationName(resultSet.getString("location_name"));
        photoVO.setCameraMake(resultSet.getString("camera_make"));
        photoVO.setCameraModel(resultSet.getString("camera_model"));
        photoVO.setFavorite(resultSet.getBoolean("is_favorite"));
        photoVO.setCreateTime(toLocalDateTime(resultSet.getTimestamp("created_at")));
        photoVO.setUpdateTime(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return photoVO;
    };

    public PersonPhotoMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PersonPhotoVO> selectListByPersonIdAndUserId(Long personId, Long userId) {
        String sql = """
                SELECT DISTINCT photo.id, photo.filename, photo.original_name, photo.file_size, photo.mime_type,
                       photo.width, photo.height, photo.shot_at, photo.latitude, photo.longitude,
                       photo.location_name, photo.camera_make, photo.camera_model, photo.is_favorite,
                       photo.created_at, photo.updated_at
                FROM pm_photo photo
                INNER JOIN pm_person person ON person.id = ?
                WHERE photo.user_id = ?
                  AND person.user_id = ?
                  AND EXISTS (
                      SELECT 1
                      FROM pm_face face
                      WHERE face.photo_id = photo.id AND face.person_id = ?
                  )
                ORDER BY COALESCE(photo.shot_at, photo.created_at) DESC, photo.id DESC
                """;
        return jdbcTemplate.query(sql, photoRowMapper, personId, userId, userId, personId);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
