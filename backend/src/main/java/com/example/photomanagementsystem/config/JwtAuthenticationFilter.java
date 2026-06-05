package com.example.photomanagementsystem.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(//只有子类或同包可以访问
                                    HttpServletRequest request,//在 JWT 过滤器中，通常用它拿 Authorization Header
                                    HttpServletResponse response,//可以用它写回响应内容、设置状态码或响应头
                                    FilterChain filterChain)//表示过滤器链，负责继续调用后续的过滤器和最终的 Controller
            throws ServletException, IOException {

        // 1. 从请求头中获取 Authorization
        String authorization = request.getHeader("Authorization");

        // 2. 判断请求头是否有 Token，并且是否以 Bearer 开头
        if (authorization != null && authorization.startsWith("Bearer")) {

            // 3. 去掉 Bearer 前缀，拿到真正的 Token
            String token = authorization.substring(7);
            try {
                // 4. 解析 Token，获取用户信息
                Claims claims = JwtUtils.parseToken(token);
                Long userId = Long.valueOf(claims.get("userId").toString());
                String userName = claims.get("userName", String.class);

                // 5. 创建 Spring Security 认证对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userName,//用户标识，通常是用户名或 UserDetails 对象
                        null,//用户密码或凭证，JWT 场景下可以不需要，所以传 null
                        Collections.emptyList());//用户角色或权限列表，JWT 校验后如果暂时不做权限控制，可以传空列表 Collections.emptyList()

                // 6. 可以把用户ID放到 details 里，后续需要时可以取
                authentication.setDetails(userId);

                // 7. 把认证信息放入 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Token 无效、过期、被篡改时，清空认证信息
                SecurityContextHolder.clearContext();

            }
        }
        // 8. 放行，继续执行后面的过滤器
        filterChain.doFilter(request, response);

    }
}