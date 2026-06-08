package com.example.photomanagementsystem.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


public class JwtUtils {

    //jwt密钥，给 Token 盖章用的私章，即token的Signature
    private static final String SECRET = "photo-manager-secret-key-photo-manager-secret-key";
    //token 有效期：7天
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;
    //把字符串密钥 SECRET 转换成 JWT 算法能使用的 SecretKey 对象
    private  static final SecretKey SECRET_KEY  = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    //生成token
    public static String generateToken(Long userId, String userName) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + EXPIRE_TIME);
        return Jwts.builder()
                .claim("userId", userId)
                .claim("userName", userName)
                .issuedAt(now)
                .expiration(expireTime)
                .signWith(SECRET_KEY)//使用 secretKey 对 JWT 进行签名,即给 Token 盖章
                .compact();//生成最终JWT字符串
    }

    //解析token
    public static Claims parseToken(String token) {
        return Jwts.parser()//创建 JWT 解析器
                .verifyWith(SECRET_KEY)//使用 KEY 验证 Token 签名
                .build()//根据前面的配置创建解析器对象
                .parseSignedClaims(token)//真正开始解析 Token
                .getPayload();

    }

    //从 token 中获取用户ID
    public static Long getuserId(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        return Long.valueOf(userId.toString());
    }

    // 从 token 中获取用户名
    public static String getuserName(String token) {
        Claims claims = parseToken(token);
        return claims.get("userName", String.class);
    }

    //判断token是否过期
    public static boolean isExpire(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }

}
