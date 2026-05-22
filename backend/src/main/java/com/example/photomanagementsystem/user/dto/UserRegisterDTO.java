package com.example.photomanagementsystem.user.dto;

import lombok.Data;

/**
 * User registration request.
 */
@Data
public class UserRegisterDTO {

    private String username;

    private String password;

    private String nickname;

    private String email;
}
