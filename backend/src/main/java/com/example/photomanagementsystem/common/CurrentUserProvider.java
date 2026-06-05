package com.example.photomanagementsystem.common;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new BizException(401, "用户未登录");
        }

        Object details = authentication.getDetails();
        if (details instanceof Long userId) {
            return userId;
        }
        if (details instanceof Number number) {
            return number.longValue();
        }
        if (details instanceof String value) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException exception) {
                throw new BizException(401, "登录用户信息无效");
            }
        }
        throw new BizException(401, "登录用户信息无效");
    }
}
