package com.example.photomanagementsystem.user.service;

import com.example.photomanagementsystem.user.dto.UserProfileUpdateDTO;
import com.example.photomanagementsystem.user.vo.UserProfileVO;

/**
 * User service.
 */
public interface UserService {

    UserProfileVO getProfile();

    UserProfileVO updateProfile(UserProfileUpdateDTO updateDTO);
}
