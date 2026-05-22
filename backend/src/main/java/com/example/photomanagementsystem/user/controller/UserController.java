package com.example.photomanagementsystem.user.controller;

import com.example.photomanagementsystem.common.Result;
import com.example.photomanagementsystem.user.dto.UserProfileUpdateDTO;
import com.example.photomanagementsystem.user.service.UserService;
import com.example.photomanagementsystem.user.vo.UserProfileVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User profile interfaces.
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile() {
        return Result.success(userService.getProfile());
    }

    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@RequestBody UserProfileUpdateDTO updateDTO) {
        return Result.success(userService.updateProfile(updateDTO));
    }
}
