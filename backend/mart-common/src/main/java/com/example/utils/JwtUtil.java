package com.example.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    /**
     * 根据秘钥字符串生成 SecretKey（JJWT 0.12.x 要求）
     */
    private static SecretKey getSecretKey(String secretKey) {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 jwt
     *
     * @param secretKey 秘钥
     * @param ttlMillis 过期时间（毫秒）
     * @param claims    自定义声明
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        return Jwts.builder()
                .claims(claims)                                   // 设置自定义声明
                .expiration(exp)                                  // 过期时间
                .signWith(getSecretKey(secretKey), Jwts.SIG.HS256) // 签名算法 + 秘钥
                .compact();
    }

    /**
     * 解析 jwt
     */
    public static Claims parseJWT(String secretKey, String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey(secretKey))   // 校验签名
                .build()
                .parseSignedClaims(token)              // 解析
                .getPayload();                         // 取 body
    }
}