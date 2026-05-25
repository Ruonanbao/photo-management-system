package com.example.photomanagementsystem.person.controller;

import com.example.photomanagementsystem.person.mapper.PersonPhotoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Person controller integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PersonControllerTest {

    private static final long TEST_USER_ID = 1L;

    private static final String TEST_PREFIX = "it_person_";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private PersonPhotoMapper personPhotoMapper;

    @BeforeEach
    void setUp() {
        ensureTestUser();
        cleanTestPersons();
        when(personPhotoMapper.selectListByPersonIdAndUserId(anyLong(), anyLong())).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        cleanTestPersons();
    }

    @Test
    void listPersonsShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getPersonShouldReturnSuccess() throws Exception {
        Long personId = insertTestPerson(uniquePersonName());

        mockMvc.perform(get("/api/v1/persons/{id}", personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(personId));
    }

    @Test
    void getPersonWhenNotExistsShouldReturnBizFailure() throws Exception {
        mockMvc.perform(get("/api/v1/persons/{id}", -1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void updatePersonShouldReturnSuccess() throws Exception {
        Long personId = insertTestPerson(uniquePersonName());
        String updatedName = uniquePersonName();

        mockMvc.perform(put("/api/v1/persons/{id}", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(personBody(updatedName))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value(updatedName));
    }

    @Test
    void updatePersonWithIllegalNameShouldReturnFailure() throws Exception {
        Long personId = insertTestPerson(uniquePersonName());

        mockMvc.perform(put("/api/v1/persons/{id}", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(personBody(" "))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void updatePersonWhenNotExistsShouldReturnBizFailure() throws Exception {
        mockMvc.perform(put("/api/v1/persons/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(personBody(uniquePersonName()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deletePersonShouldReturnSuccess() throws Exception {
        Long personId = insertTestPerson(uniquePersonName());

        mockMvc.perform(delete("/api/v1/persons/{id}", personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM pm_person WHERE id = ?", Integer.class, personId);
        org.assertj.core.api.Assertions.assertThat(count).isZero();
    }

    @Test
    void deletePersonWhenNotExistsShouldReturnBizFailure() throws Exception {
        mockMvc.perform(delete("/api/v1/persons/{id}", -1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void listPersonPhotosShouldReturnSuccess() throws Exception {
        Long personId = insertTestPerson(uniquePersonName());

        mockMvc.perform(get("/api/v1/persons/{id}/photos", personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listPersonsShouldContainCreatedPerson() throws Exception {
        insertTestPerson(uniquePersonName());

        mockMvc.perform(get("/api/v1/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data[0].id", notNullValue()));
    }

    private void ensureTestUser() {
        jdbcTemplate.update("""
                INSERT INTO sys_user (id, username, password, nickname, role, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """, TEST_USER_ID, "it_user_1", "test_password", "Integration Test User", "USER", 1,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Long insertTestPerson(String name) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO pm_person (user_id, name, cover_face_id, created_at, updated_at)
                VALUES (?, ?, NULL, ?, ?)
                RETURNING id
                """, Long.class, TEST_USER_ID, name, LocalDateTime.now(), LocalDateTime.now());
    }

    private void cleanTestPersons() {
        jdbcTemplate.update("""
                UPDATE pm_face
                SET person_id = NULL
                WHERE person_id IN (
                    SELECT id FROM pm_person WHERE user_id = ? AND name LIKE ?
                )
                """, TEST_USER_ID, TEST_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM pm_person WHERE user_id = ? AND name LIKE ?", TEST_USER_ID, TEST_PREFIX + "%");
    }

    private Map<String, Object> personBody(String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        return body;
    }

    private String uniquePersonName() {
        return TEST_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
