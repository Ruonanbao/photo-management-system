package com.example.photomanagementsystem.user.dto;

import lombok.Data;

/**
 * User profile update request.
 */
@Data
public class UserProfileUpdateDTO {

    private String nickname;

    private String email;

    private String avatar;
}
