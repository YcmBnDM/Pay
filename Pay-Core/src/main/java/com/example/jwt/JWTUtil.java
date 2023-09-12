package com.example.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * JWT 工具箱
 */
public class JWTUtil {

    /**
     * 生成 Token
     *
     * @param jwtPayload
     * @param jwtSecret
     * @return
     */
    public static String generateToken(JWTPayload jwtPayload, String jwtSecret) {
        return Jwts.builder()
                .setClaims(jwtPayload.toMap())
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }


    /**
     * 解析Token
     * @param token
     * @param secret
     * @return
     */
    public static JWTPayload parseToken(String token, String secret) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            JWTPayload result = new JWTPayload();
            result.setSysUserId(claims.get("sysUserId", Long.class));
            result.setCreated(claims.get("created", Long.class));
            result.setCacheKey(claims.get("cacheKey", String.class));
            return result;
        } catch (Exception e) {
            return null;
        }

    }
}
