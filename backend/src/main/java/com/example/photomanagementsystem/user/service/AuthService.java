package com.example.photomanagementsystem.user.service;

import com.example.photomanagementsystem.user.dto.UserLoginDTO;
import com.example.photomanagementsystem.user.dto.UserRegisterDTO;
import com.example.photomanagementsystem.user.vo.UserLoginVO;
import com.example.photomanagementsystem.user.vo.UserProfileVO;

/**
 * Authentication service.
 */
public interface AuthService {

    UserProfileVO register(UserRegisterDTO registerDTO);

    UserLoginVO login(UserLoginDTO loginDTO);

    void logout();
}
