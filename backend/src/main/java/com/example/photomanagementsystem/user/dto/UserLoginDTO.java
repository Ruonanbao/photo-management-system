package com.example.photomanagementsystem.user.dto;

import lombok.Data;

/**
 * User login request.
 */
@Data
public class UserLoginDTO {

    private String username;

    private String password;
}
