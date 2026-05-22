package com.example.photomanagementsystem.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * User profile response.
 */
@Data
public class UserProfileVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String avatar;

    private String role;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
