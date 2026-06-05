package com.example.photomanagementsystem.testsupport;

import com.example.photomanagementsystem.config.JwtUtils;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class TestJwtSupport {

    private TestJwtSupport() {
    }

    public static RequestPostProcessor jwt(long userId) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + JwtUtils.generateToken(userId, "it_user_" + userId));
            return request;
        };
    }
}
