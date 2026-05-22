package com.example.photomanagementsystem.user.vo;

import lombok.Data;

/**
 * User login response.
 */
@Data
public class UserLoginVO {

    private String token;

    private UserProfileVO user;
}
