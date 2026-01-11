package com.yizhcr.my_blog.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {
    // Token有效期：5小时（毫秒）
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000;

    // JWT秘钥
    @Value("${jwt.secret}")
    private String secret;

    // 从Token中获取用户名
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // 从Token中获取过期时间
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // 从Token中获取所有Claims
    public Claims getAllClaimsFromToken(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 从Token中解析指定Claim
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 从当前请求中获取Token
    public String getTokenFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }

            HttpServletRequest request = attributes.getRequest();
            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                return header.substring(7);
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // 检查Token是否过期
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // 为用户生成Token
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    // 生成Token的核心逻辑（使用新的signWith API）
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        // 1. 将字符串秘钥转为SecretKey
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        // 2. 使用新的API构建Token，替代旧的signWith(SignatureAlgorithm, String)
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(secretKey) // 无需指定算法，密钥自动匹配（HS512）
                .compact();
    }

    // 验证Token有效性（匹配用户名 + 未过期）
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // 验证Token是否有效（仅检查过期时间，不验证用户名）
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}