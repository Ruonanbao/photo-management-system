package com.example.photomanagementsystem.user.service.impl;

import com.example.photomanagementsystem.common.BizException;
import com.example.photomanagementsystem.user.dto.UserLoginDTO;
import com.example.photomanagementsystem.user.dto.UserRegisterDTO;
import com.example.photomanagementsystem.user.entity.SysUser;
import com.example.photomanagementsystem.user.mapper.SysUserMapper;
import com.example.photomanagementsystem.user.service.AuthService;
import com.example.photomanagementsystem.user.vo.UserLoginVO;
import com.example.photomanagementsystem.user.vo.UserProfileVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Authentication service implementation.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final int USERNAME_MAX_LENGTH = 50;

    private static final int PASSWORD_MIN_LENGTH = 6;

    private static final int NICKNAME_MAX_LENGTH = 50;

    private static final int EMAIL_MAX_LENGTH = 100;

    private static final String DEFAULT_ROLE = "USER";

    private static final int ENABLED_STATUS = 1;

    private final SysUserMapper sysUserMapper;

    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO register(UserRegisterDTO registerDTO) {
        String username = registerDTO == null ? null : registerDTO.getUsername();
        String password = registerDTO == null ? null : registerDTO.getPassword();
        validateUsername(username);
        validatePassword(password);
        validateProfile(registerDTO.getNickname(), registerDTO.getEmail());

        String trimmedUsername = username.trim();
        if (sysUserMapper.existsByUsername(trimmedUsername)) {
            throw new BizException(400, "用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        SysUser user = new SysUser();
        user.setUsername(trimmedUsername);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(trimToNull(registerDTO.getNickname()));
        user.setEmail(trimToNull(registerDTO.getEmail()));
        user.setRole(DEFAULT_ROLE);
        user.setStatus(ENABLED_STATUS);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        return convertToUserProfileVO(sysUserMapper.insert(user));
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        String username = loginDTO == null ? null : loginDTO.getUsername();
        String password = loginDTO == null ? null : loginDTO.getPassword();
        validateUsername(username);
        if (!StringUtils.hasText(password)) {
            throw new BizException(400, "密码不能为空");
        }

        SysUser user = sysUserMapper.selectByUsername(username.trim())
                .orElseThrow(() -> new BizException(401, "用户名或密码错误"));
        if (!isEnabledStatus(user.getStatus())) {
            throw new BizException(403, "用户已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }

        UserLoginVO loginVO = new UserLoginVO();
        loginVO.setToken(buildToken(user));
        loginVO.setUser(convertToUserProfileVO(user));
        return loginVO;
    }

    @Override
    public void logout() {
        // Stateless placeholder. Replace with token blacklist when JWT is introduced.
    }

    private void validateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BizException(400, "用户名不能为空");
        }
        if (username.trim().length() > USERNAME_MAX_LENGTH) {
            throw new BizException(400, "用户名长度不能超过50");
        }
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new BizException(400, "密码不能为空");
        }
        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new BizException(400, "密码长度不能少于6");
        }
    }

    private void validateProfile(String nickname, String email) {
        if (StringUtils.hasText(nickname) && nickname.trim().length() > NICKNAME_MAX_LENGTH) {
            throw new BizException(400, "昵称长度不能超过50");
        }
        if (StringUtils.hasText(email) && email.trim().length() > EMAIL_MAX_LENGTH) {
            throw new BizException(400, "邮箱长度不能超过100");
        }
    }

    private String buildToken(SysUser user) {
        String value = user.getId() + ":" + UUID.randomUUID();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isEnabledStatus(Integer status) {
        return Integer.valueOf(ENABLED_STATUS).equals(status);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private UserProfileVO convertToUserProfileVO(SysUser user) {
        UserProfileVO profileVO = new UserProfileVO();
        profileVO.setId(user.getId());
        profileVO.setUsername(user.getUsername());
        profileVO.setNickname(user.getNickname());
        profileVO.setEmail(user.getEmail());
        profileVO.setAvatar(user.getAvatar());
        profileVO.setRole(user.getRole());
        profileVO.setStatus(user.getStatus());
        profileVO.setCreateTime(user.getCreateTime());
        profileVO.setUpdateTime(user.getUpdateTime());
        return profileVO;
    }
}
