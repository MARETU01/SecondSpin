package com.secondspin.common.utils;

import com.secondspin.common.dto.JwtUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    private static final String SECRET = "secondspinsecondspinsecondspinaa";
    private static final SecretKey signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final Long expirationTime = 86400000L;

    private static Map<String, Object> generateClaim(JwtUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("userId", user.getUserId());
        claims.put("email", user.getEmail());
        return claims;
    }

    public static String generateJwt(JwtUser user) {
        return Jwts.builder()
                .claims(generateClaim(user))
                .signWith(signingKey)
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public static String logOutJwt(JwtUser user) {
        return Jwts.builder()
                .claims(generateClaim(user))
                .signWith(signingKey)
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public static JwtUser parseJwt(String jwt) {
        Claims claims =  Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(jwt).getPayload();
        JwtUser user = new JwtUser();
        user.setUserId(Integer.parseInt(claims.get("userId").toString()));
        user.setUsername(claims.get("username", String.class));
        user.setEmail(claims.get("email", String.class));
        return user;
    }
}