package com.example.photomanagementsystem.user.controller;

import com.example.photomanagementsystem.common.BizException;
import com.example.photomanagementsystem.user.service.UserService;
import com.example.photomanagementsystem.user.vo.UserProfileVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * User profile controller tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getProfileShouldReturnSuccess() throws Exception {
        Mockito.when(userService.getProfile()).thenReturn(profileVO());

        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    void getProfileWhenUserNotExistsShouldReturnBizFailure() throws Exception {
        Mockito.when(userService.getProfile()).thenThrow(new BizException(404, "user not found"));

        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void updateProfileShouldReturnSuccess() throws Exception {
        Mockito.when(userService.updateProfile(any())).thenReturn(profileVO());

        mockMvc.perform(put("/api/v1/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(profileBody("Integration Test User"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("it_profile_user"));
    }

    @Test
    void updateProfileWithIllegalArgumentShouldReturnFailure() throws Exception {
        Mockito.when(userService.updateProfile(any())).thenThrow(new BizException(400, "invalid profile"));

        mockMvc.perform(put("/api/v1/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(profileBody("a".repeat(51)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    private UserProfileVO profileVO() {
        UserProfileVO profileVO = new UserProfileVO();
        profileVO.setId(1L);
        profileVO.setUsername("it_profile_user");
        profileVO.setNickname("Integration Test User");
        profileVO.setEmail("it_profile_user@example.com");
        profileVO.setRole("USER");
        profileVO.setStatus(1);
        return profileVO;
    }

    private Map<String, Object> profileBody(String nickname) {
        Map<String, Object> body = new HashMap<>();
        body.put("nickname", nickname);
        body.put("email", "it_profile_user@example.com");
        body.put("avatar", "https://example.com/avatar.png");
        return body;
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
