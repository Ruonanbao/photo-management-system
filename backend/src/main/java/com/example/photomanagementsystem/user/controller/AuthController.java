package com.example.photomanagementsystem.user.controller;

import com.example.photomanagementsystem.common.Result;
import com.example.photomanagementsystem.user.dto.UserLoginDTO;
import com.example.photomanagementsystem.user.dto.UserRegisterDTO;
import com.example.photomanagementsystem.user.service.AuthService;
import com.example.photomanagementsystem.user.vo.UserLoginVO;
import com.example.photomanagementsystem.user.vo.UserProfileVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication interfaces.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<UserProfileVO> register(@RequestBody UserRegisterDTO registerDTO) {
        return Result.success(authService.register(registerDTO));
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
