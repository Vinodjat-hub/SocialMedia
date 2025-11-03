package com.dollop.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${jwt.access.secret}")
    private String accessSecretB64;

    @Value("${jwt.temp.secret}")
    private String tempSecretB64;

    @Value("${jwt.access.ttl}")
    private long accessTtlMs;

    @Value("${jwt.temp.ttl}")
    private long tempTtlMs;

    private SecretKey accessKey; // HS512 (>= 64 bytes)
    private SecretKey tempKey;   // HS512 (>= 64 bytes)

    @jakarta.annotation.PostConstruct
    void initKeys() {
        try {
            // Access token key
            byte[] accessBytes = Decoders.BASE64.decode(accessSecretB64);
            if (accessBytes.length < 32) {
                throw new IllegalArgumentException("Access secret is too short, must be at least 256 bits (32 bytes)");
            }
            this.accessKey = Keys.hmacShaKeyFor(accessBytes);

            // Temp token key
            byte[] tempBytes = Decoders.BASE64.decode(tempSecretB64);
            if (tempBytes.length < 32) {
                throw new IllegalArgumentException("Temp secret is too short, must be at least 256 bits (32 bytes)");
            }
            this.tempKey = Keys.hmacShaKeyFor(tempBytes);

            System.out.println("JWT keys initialized successfully");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to initialize JWT keys: " + e.getMessage(), e);
        }
    }


    /* ===================== ACCESS TOKEN ===================== */

    public String generateAccessToken(String subject) {
        return generateAccessToken(subject, new HashMap<>());
    }

    public String generateAccessToken(String subject, Map<String, Object> customClaims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTtlMs);

        return Jwts.builder()
                .subject(subject)
                .claims(customClaims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(accessKey)              // HS512 inferred from key size
                .compact();
    }
    
    public String generateToken(String subject, String type) {
        return generateToken(subject, new HashMap<>(),type);
    }
    
    public String generateToken(String subject, Map<String, Object> customClaims, String type) {
        Date now = new Date();
        Date exp = null;
        if("otpToken".equals(type))
           exp = new Date(now.getTime() + tempTtlMs);
        if("loginToken".equals(type))
        exp = new Date(now.getTime() + accessTtlMs);

        Map<String, Object> claims = new HashMap<>(customClaims);
        claims.put("token_type", type);

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(accessKey) // e.g., Keys.hmacShaKeyFor(secret.getBytes())
                .compact();
    }


    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(accessKey)           // <-- SAME KEY used to sign
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    

    public String getSubjectFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /* ===================== TEMP / OTP TOKEN ===================== */

    public String generateTempToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + tempTtlMs);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(exp)
                .signWith(tempKey)
                .compact();
    }

    public boolean validateTempToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(tempKey)            // <-- SAME KEY used to sign TEMP token
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromTempToken(String tempToken) {
        Claims claims = Jwts.parser()
                .verifyWith(tempKey)
                .build()
                .parseSignedClaims(tempToken)
                .getPayload();
        return claims.getSubject(); // email stored in "sub"
    }
}
