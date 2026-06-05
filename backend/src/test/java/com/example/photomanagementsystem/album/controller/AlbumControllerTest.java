package com.example.photomanagementsystem.album.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static com.example.photomanagementsystem.testsupport.TestJwtSupport.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Album controller integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AlbumControllerTest {

    private static final long TEST_USER_ID = 1L;
    private static final long OTHER_USER_ID = 2L;

    private static final String TEST_PREFIX = "it_album_";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        ensureTestUser();
        ensureOtherUser();
        cleanTestAlbums();
    }

    @AfterEach
    void tearDown() {
        cleanTestAlbums();
    }

    @Test
    void listAlbumsShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/albums").with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void createAlbumShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/albums")
                        .with(jwt(TEST_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createAlbumBody(uniqueAlbumName(), "integration test album"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.name").value(org.hamcrest.Matchers.startsWith(TEST_PREFIX)));
    }

    @Test
    void createAlbumWithoutNameShouldReturnFailure() throws Exception {
        mockMvc.perform(post("/api/v1/albums")
                        .with(jwt(TEST_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createAlbumBody(" ", "missing name"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getAlbumWhenNotExistsShouldReturnBizFailure() throws Exception {
        mockMvc.perform(get("/api/v1/albums/{id}", -1L).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void updateAlbumShouldReturnSuccess() throws Exception {
        Long albumId = insertTestAlbum(uniqueAlbumName());
        String updatedName = uniqueAlbumName();

        mockMvc.perform(put("/api/v1/albums/{id}", albumId)
                        .with(jwt(TEST_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createAlbumBody(updatedName, "updated by integration test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value(updatedName));
    }

    @Test
    void updateAlbumWithIllegalNameShouldReturnFailure() throws Exception {
        Long albumId = insertTestAlbum(uniqueAlbumName());
        String illegalName = "a".repeat(51);

        mockMvc.perform(put("/api/v1/albums/{id}", albumId)
                        .with(jwt(TEST_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createAlbumBody(illegalName, "illegal name"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void deleteAlbumShouldReturnSuccess() throws Exception {
        Long albumId = insertTestAlbum(uniqueAlbumName());

        mockMvc.perform(delete("/api/v1/albums/{id}", albumId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM pm_album WHERE id = ?", Integer.class, albumId);
        org.assertj.core.api.Assertions.assertThat(count).isZero();
    }

    @Test
    void deleteAlbumWhenNotExistsShouldReturnBizFailure() throws Exception {
        mockMvc.perform(delete("/api/v1/albums/{id}", -1L).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void listAlbumPhotosShouldReturnSuccess() throws Exception {
        Long albumId = insertTestAlbum(uniqueAlbumName());

        mockMvc.perform(get("/api/v1/albums/{id}/photos", albumId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void addPhotoToAlbumWithoutPhotoIdShouldReturnFailure() throws Exception {
        Long albumId = insertTestAlbum(uniqueAlbumName());

        mockMvc.perform(post("/api/v1/albums/{id}/photos", albumId)
                        .with(jwt(TEST_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new HashMap<>())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void addPhotoToAlbumWhenPhotoNotExistsShouldReturnBizFailure() throws Exception {
        Long albumId = insertTestAlbum(uniqueAlbumName());
        Map<String, Object> body = new HashMap<>();
        body.put("photoId", -1L);

        mockMvc.perform(post("/api/v1/albums/{id}/photos", albumId)
                        .with(jwt(TEST_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void removePhotoFromAlbumWhenRelationNotExistsShouldReturnBizFailure() throws Exception {
        Long albumId = insertTestAlbum(uniqueAlbumName());

        mockMvc.perform(delete("/api/v1/albums/{id}/photos/{photoId}", albumId, -1L).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void listAlbumsShouldContainCreatedAlbum() throws Exception {
        String albumName = uniqueAlbumName();
        insertTestAlbum(albumName);

        mockMvc.perform(get("/api/v1/albums").with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()", greaterThan(0)));
    }

    @Test
    void getOtherUserAlbumShouldReturnBizFailure() throws Exception {
        Long albumId = insertTestAlbum(OTHER_USER_ID, uniqueAlbumName());

        mockMvc.perform(get("/api/v1/albums/{id}", albumId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void updateOtherUserAlbumShouldReturnBizFailure() throws Exception {
        Long albumId = insertTestAlbum(OTHER_USER_ID, uniqueAlbumName());

        mockMvc.perform(put("/api/v1/albums/{id}", albumId)
                        .with(jwt(TEST_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createAlbumBody(uniqueAlbumName(), "cross user update"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteOtherUserAlbumShouldReturnBizFailure() throws Exception {
        Long albumId = insertTestAlbum(OTHER_USER_ID, uniqueAlbumName());

        mockMvc.perform(delete("/api/v1/albums/{id}", albumId).with(jwt(TEST_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM pm_album WHERE id = ?", Integer.class, albumId);
        org.assertj.core.api.Assertions.assertThat(count).isOne();
    }

    private void ensureTestUser() {
        ensureUser(TEST_USER_ID, "it_user_1");
    }

    private void ensureOtherUser() {
        ensureUser(OTHER_USER_ID, "it_user_2");
    }

    private void ensureUser(long userId, String username) {
        jdbcTemplate.update("""
                INSERT INTO sys_user (id, username, password, nickname, role, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """, userId, username, "test_password", "Integration Test User", "USER", 1,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Long insertTestAlbum(String name) {
        return insertTestAlbum(TEST_USER_ID, name);
    }

    private Long insertTestAlbum(long userId, String name) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO pm_album
                    (user_id, name, description, cover_photo_id, is_default, is_deleted, created_at, updated_at)
                VALUES (?, ?, ?, NULL, FALSE, FALSE, ?, ?)
                RETURNING id
                """, Long.class, userId, name, "integration test album", LocalDateTime.now(), LocalDateTime.now());
    }

    private void cleanTestAlbums() {
        jdbcTemplate.update("""
                DELETE FROM pm_album_photo
                WHERE album_id IN (
                    SELECT id FROM pm_album WHERE user_id IN (?, ?) AND name LIKE ?
                )
                """, TEST_USER_ID, OTHER_USER_ID, TEST_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM pm_album WHERE user_id IN (?, ?) AND name LIKE ?",
                TEST_USER_ID, OTHER_USER_ID, TEST_PREFIX + "%");
    }

    private Map<String, Object> createAlbumBody(String name, String description) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description);
        return body;
    }

    private String uniqueAlbumName() {
        return TEST_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
