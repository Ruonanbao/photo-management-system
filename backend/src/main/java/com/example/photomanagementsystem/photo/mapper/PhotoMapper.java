package com.example.photomanagementsystem.photo.mapper;

import com.example.photomanagementsystem.photo.dto.PhotoListQueryDTO;
import com.example.photomanagementsystem.photo.entity.Photo;
import com.example.photomanagementsystem.photo.vo.PersonPhotoRowVO;
import com.example.photomanagementsystem.photo.vo.PhotoVO;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Photo data access for pm_photo.
 */
@Repository("photoModulePhotoMapper")
public class PhotoMapper {

    private static final String PHOTO_COLUMNS = """
            id, user_id, filename, original_name, file_path, thumbnail_path, file_size, mime_type,
            width, height, shot_at, latitude, longitude, location_name, camera_make, camera_model,
            is_favorite, created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Photo> photoRowMapper = (resultSet, rowNum) -> {
        Photo photo = new Photo();
        photo.setId(resultSet.getLong("id"));
        photo.setUserId(resultSet.getLong("user_id"));
        photo.setFilename(resultSet.getString("filename"));
        photo.setOriginalName(resultSet.getString("original_name"));
        photo.setFilePath(resultSet.getString("file_path"));
        photo.setThumbnailPath(resultSet.getString("thumbnail_path"));
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

    public Photo insert(Photo photo) {
        String sql = """
                INSERT INTO pm_photo
                    (user_id, filename, original_name, file_path, thumbnail_path, file_size, mime_type, width, height,
                     shot_at, latitude, longitude, location_name, camera_make, camera_model, is_favorite,
                     created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        Long id = jdbcTemplate.queryForObject(sql, Long.class, photo.getUserId(), photo.getFilename(),
                photo.getOriginalName(), photo.getFilePath(), photo.getThumbnailPath(), photo.getFileSize(),
                photo.getMimeType(), photo.getWidth(), photo.getHeight(), photo.getShotAt(), photo.getLatitude(),
                photo.getLongitude(), photo.getLocationName(), photo.getCameraMake(), photo.getCameraModel(),
                photo.getFavorite(), photo.getCreateTime(), photo.getUpdateTime());
        photo.setId(id);
        return photo;
    }

    public List<Photo> selectPageByUserId(Long userId, PhotoListQueryDTO queryDTO, int offset, int size) {
        SqlCondition condition = buildListCondition(userId, queryDTO);
        String sql = """
                SELECT %s
                FROM pm_photo photo
                %s
                ORDER BY COALESCE(shot_at, created_at) DESC, id DESC
                LIMIT ? OFFSET ?
                """.formatted(PHOTO_COLUMNS, condition.whereSql());
        List<Object> params = new ArrayList<>(condition.params());
        params.add(size);
        params.add(offset);
        return jdbcTemplate.query(sql, photoRowMapper, params.toArray());
    }

    public long countByUserId(Long userId, PhotoListQueryDTO queryDTO) {
        SqlCondition condition = buildListCondition(userId, queryDTO);
        String sql = "SELECT COUNT(1) FROM pm_photo photo " + condition.whereSql();
        Long count = jdbcTemplate.queryForObject(sql, Long.class, condition.params().toArray());
        return count == null ? 0L : count;
    }

    public Optional<Photo> selectByIdAndUserId(Long id, Long userId) {
        String sql = "SELECT %s FROM pm_photo WHERE id = ? AND user_id = ?".formatted(PHOTO_COLUMNS);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, photoRowMapper, id, userId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public void updateFavorite(Long id, Long userId, Boolean favorite, LocalDateTime updateTime) {
        String sql = "UPDATE pm_photo SET is_favorite = ?, updated_at = ? WHERE id = ? AND user_id = ?";
        jdbcTemplate.update(sql, favorite, updateTime, id, userId);
    }

    public void updateThumbnailPath(Long id, Long userId, String thumbnailPath, LocalDateTime updateTime) {
        String sql = "UPDATE pm_photo SET thumbnail_path = ?, updated_at = ? WHERE id = ? AND user_id = ?";
        jdbcTemplate.update(sql, thumbnailPath, updateTime, id, userId);
    }

    public void deleteByIdAndUserId(Long id, Long userId) {
        String sql = "DELETE FROM pm_photo WHERE id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }

    public List<Photo> selectTimelineByUserId(Long userId) {
        String sql = """
                SELECT %s
                FROM pm_photo
                WHERE user_id = ?
                ORDER BY COALESCE(shot_at, created_at) DESC, id DESC
                """.formatted(PHOTO_COLUMNS);
        return jdbcTemplate.query(sql, photoRowMapper, userId);
    }

    public List<Photo> selectLocationsByUserId(Long userId) {
        String sql = """
                SELECT %s
                FROM pm_photo
                WHERE user_id = ?
                ORDER BY COALESCE(NULLIF(location_name, ''), '未知地点') ASC,
                         COALESCE(shot_at, created_at) DESC, id DESC
                """.formatted(PHOTO_COLUMNS);
        return jdbcTemplate.query(sql, photoRowMapper, userId);
    }

    public List<PersonPhotoRowVO> selectPeoplePhotosByUserId(Long userId) {
        String sql = """
                SELECT person.id AS person_id, person.name AS person_name, person.cover_face_id,
                       photo.id, photo.user_id, photo.filename, photo.original_name, photo.file_path,
                       photo.thumbnail_path, photo.file_size, photo.mime_type, photo.width, photo.height,
                       photo.shot_at, photo.latitude, photo.longitude, photo.location_name, photo.camera_make,
                       photo.camera_model, photo.is_favorite, photo.created_at, photo.updated_at
                FROM pm_person person
                INNER JOIN pm_face face ON face.person_id = person.id
                INNER JOIN pm_photo photo ON photo.id = face.photo_id
                WHERE person.user_id = ? AND photo.user_id = ?
                ORDER BY person.id ASC, COALESCE(photo.shot_at, photo.created_at) DESC, photo.id DESC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            PersonPhotoRowVO row = new PersonPhotoRowVO();
            row.setPersonId(resultSet.getLong("person_id"));
            row.setPersonName(resultSet.getString("person_name"));
            row.setCoverFaceId(resultSet.getObject("cover_face_id", Long.class));
            row.setPhoto(toPhotoVO(photoRowMapper.mapRow(resultSet, rowNum)));
            return row;
        }, userId, userId);
    }

    private SqlCondition buildListCondition(Long userId, PhotoListQueryDTO queryDTO) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        conditions.add("photo.user_id = ?");
        params.add(userId);

        if (queryDTO != null && queryDTO.getFavorite() != null) {
            conditions.add("photo.is_favorite = ?");
            params.add(queryDTO.getFavorite());
        }
        if (queryDTO != null && StringUtils.hasText(queryDTO.getKeyword())) {
            conditions.add("""
                    (LOWER(photo.filename) LIKE LOWER(?)
                    OR LOWER(COALESCE(photo.original_name, '')) LIKE LOWER(?)
                    OR LOWER(COALESCE(photo.location_name, '')) LIKE LOWER(?)
                    OR EXISTS (
                        SELECT 1
                        FROM pm_album_photo relation
                        INNER JOIN pm_album album ON album.id = relation.album_id
                        WHERE relation.photo_id = photo.id
                          AND album.user_id = photo.user_id
                          AND album.is_deleted = FALSE
                          AND LOWER(album.name) LIKE LOWER(?)
                    )
                    OR EXISTS (
                        SELECT 1
                        FROM pm_face face
                        INNER JOIN pm_person person ON person.id = face.person_id
                        WHERE face.photo_id = photo.id
                          AND person.user_id = photo.user_id
                          AND LOWER(COALESCE(person.name, '')) LIKE LOWER(?)
                    ))
                    """);
            String keyword = "%" + queryDTO.getKeyword().trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }
        if (queryDTO != null && StringUtils.hasText(queryDTO.getLocationName())) {
            conditions.add("LOWER(COALESCE(photo.location_name, '')) LIKE LOWER(?)");
            params.add("%" + queryDTO.getLocationName().trim() + "%");
        }
        if (queryDTO != null && queryDTO.getAlbumId() != null) {
            conditions.add("""
                    EXISTS (
                        SELECT 1
                        FROM pm_album_photo relation
                        INNER JOIN pm_album album ON album.id = relation.album_id
                        WHERE relation.photo_id = photo.id
                          AND relation.album_id = ?
                          AND album.user_id = photo.user_id
                          AND album.is_deleted = FALSE
                    )
                    """);
            params.add(queryDTO.getAlbumId());
        }
        if (queryDTO != null && queryDTO.getPersonId() != null) {
            conditions.add("""
                    EXISTS (
                        SELECT 1
                        FROM pm_face face
                        INNER JOIN pm_person person ON person.id = face.person_id
                        WHERE face.photo_id = photo.id
                          AND face.person_id = ?
                          AND person.user_id = photo.user_id
                    )
                    """);
            params.add(queryDTO.getPersonId());
        }
        if (queryDTO != null && StringUtils.hasText(queryDTO.getStartTime())) {
            conditions.add("COALESCE(photo.shot_at, photo.created_at) >= ?");
            params.add(parseDateTime(queryDTO.getStartTime(), "开始时间格式不正确"));
        }
        if (queryDTO != null && StringUtils.hasText(queryDTO.getEndTime())) {
            conditions.add("COALESCE(photo.shot_at, photo.created_at) <= ?");
            params.add(parseDateTime(queryDTO.getEndTime(), "结束时间格式不正确"));
        }
        return new SqlCondition("WHERE " + String.join(" AND ", conditions), params);
    }

    private LocalDateTime parseDateTime(String value, String message) {
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new com.example.photomanagementsystem.common.BizException(400, message);
        }
    }

    private PhotoVO toPhotoVO(Photo photo) {
        PhotoVO photoVO = new PhotoVO();
        photoVO.setId(photo.getId());
        photoVO.setFilename(photo.getFilename());
        photoVO.setOriginalName(photo.getOriginalName());
        photoVO.setFileSize(photo.getFileSize());
        photoVO.setMimeType(photo.getMimeType());
        photoVO.setWidth(photo.getWidth());
        photoVO.setHeight(photo.getHeight());
        photoVO.setShotAt(photo.getShotAt());
        photoVO.setLatitude(photo.getLatitude());
        photoVO.setLongitude(photo.getLongitude());
        photoVO.setLocationName(photo.getLocationName());
        photoVO.setCameraMake(photo.getCameraMake());
        photoVO.setCameraModel(photo.getCameraModel());
        photoVO.setFavorite(photo.getFavorite());
        photoVO.setCreateTime(photo.getCreateTime());
        photoVO.setUpdateTime(photo.getUpdateTime());
        return photoVO;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private record SqlCondition(String whereSql, List<Object> params) {
    }
}
