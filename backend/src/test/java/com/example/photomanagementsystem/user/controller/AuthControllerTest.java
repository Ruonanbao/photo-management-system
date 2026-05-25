package com.example.photomanagementsystem.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authentication controller integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    private static final String TEST_PREFIX = "it_auth_";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        cleanTestUsers();
        resetUserIdSequence();
    }

    @AfterEach
    void tearDown() {
        cleanTestUsers();
    }

    @Test
    void registerShouldReturnSuccess() throws Exception {
        String username = uniqueUsername();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerBody(username, "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.username").value(username));
    }

    @Test
    void registerWithoutUsernameShouldReturnFailure() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerBody(" ", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void registerWithShortPasswordShouldReturnFailure() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerBody(uniqueUsername(), "123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void loginShouldReturnSuccess() throws Exception {
        String username = uniqueUsername();
        insertTestUser(username, "password123", 1);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginBody(username, "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.user.username").value(username));
    }

    @Test
    void loginWhenUserNotExistsShouldReturnBizFailure() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginBody(uniqueUsername(), "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void loginWithWrongPasswordShouldReturnBizFailure() throws Exception {
        String username = uniqueUsername();
        insertTestUser(username, "password123", 1);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginBody(username, "wrong-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void logoutShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private void insertTestUser(String username, String rawPassword, int status) {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                INSERT INTO sys_user (username, password, nickname, email, role, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, username, passwordEncoder.encode(rawPassword), username, username + "@example.com",
                "USER", status, now, now);
    }

    private void cleanTestUsers() {
        jdbcTemplate.update("DELETE FROM sys_user WHERE username LIKE ?", TEST_PREFIX + "%");
    }

    private void resetUserIdSequence() {
        jdbcTemplate.execute("""
                SELECT setval(
                    pg_get_serial_sequence('sys_user', 'id'),
                    COALESCE((SELECT MAX(id) FROM sys_user), 1),
                    true
                )
                """);
    }

    private Map<String, Object> registerBody(String username, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        body.put("nickname", username);
        body.put("email", username + "@example.com");
        return body;
    }

    private Map<String, Object> loginBody(String username, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        return body;
    }

    private String uniqueUsername() {
        return TEST_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
