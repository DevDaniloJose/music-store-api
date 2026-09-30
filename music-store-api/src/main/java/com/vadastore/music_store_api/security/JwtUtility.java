package com.vadastore.music_store_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HexFormat;
import java.util.Map;

@Service
public class JwtUtility {

    @Value("${com.music-store.jwt.secret}")
    private String SECRET_KEY;


    public String generateToken(Map<String, String> claims, String username, long expireInterval) {
        return Jwts.builder()
                .subject(username)
                .claims()
                .add(claims)
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireInterval))
                .signWith(getSignInKey())
                .compact();
    }

    public String getUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public Claims extractAllClaims(String token) {
         return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        Claims claims = extractAllClaims(token);

        return claims.getExpiration().before(new Date());
    }


    public SecretKey getSignInKey() {
        byte[] keyBytes = HexFormat.of().parseHex(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);

    }

}

