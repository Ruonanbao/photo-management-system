package com.example.photomanagementsystem.user.service.impl;

import com.example.photomanagementsystem.common.BizException;
import com.example.photomanagementsystem.common.CurrentUserProvider;
import com.example.photomanagementsystem.user.dto.UserProfileUpdateDTO;
import com.example.photomanagementsystem.user.entity.SysUser;
import com.example.photomanagementsystem.user.mapper.SysUserMapper;
import com.example.photomanagementsystem.user.service.UserService;
import com.example.photomanagementsystem.user.vo.UserProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * User service implementation.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final int NICKNAME_MAX_LENGTH = 50;

    private static final int EMAIL_MAX_LENGTH = 100;

    private final SysUserMapper sysUserMapper;
    private final CurrentUserProvider currentUserProvider;

    public UserServiceImpl(SysUserMapper sysUserMapper, CurrentUserProvider currentUserProvider) {
        this.sysUserMapper = sysUserMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public UserProfileVO getProfile() {
        return convertToUserProfileVO(getCurrentUser());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateProfile(UserProfileUpdateDTO updateDTO) {
        validateProfile(updateDTO);
        SysUser user = getCurrentUser();
        user.setNickname(trimToNull(updateDTO.getNickname()));
        user.setEmail(trimToNull(updateDTO.getEmail()));
        user.setAvatar(trimToNull(updateDTO.getAvatar()));
        user.setUpdateTime(LocalDateTime.now());
        return convertToUserProfileVO(sysUserMapper.updateProfile(user));
    }

    private SysUser getCurrentUser() {
        return sysUserMapper.selectById(getCurrentUserId())
                .orElseThrow(() -> new BizException(404, "用户不存在"));
    }

    private Long getCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    private void validateProfile(UserProfileUpdateDTO updateDTO) {
        if (updateDTO == null) {
            throw new BizException(400, "用户信息不能为空");
        }
        if (StringUtils.hasText(updateDTO.getNickname()) && updateDTO.getNickname().trim().length() > NICKNAME_MAX_LENGTH) {
            throw new BizException(400, "昵称长度不能超过50");
        }
        if (StringUtils.hasText(updateDTO.getEmail()) && updateDTO.getEmail().trim().length() > EMAIL_MAX_LENGTH) {
            throw new BizException(400, "邮箱长度不能超过100");
        }
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
