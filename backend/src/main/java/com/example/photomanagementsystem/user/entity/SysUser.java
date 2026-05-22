package com.example.photomanagementsystem.user.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * User entity for sys_user.
 */
@Data
public class SysUser {

    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String avatar;

    private String role;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
