package com.ecommerce.ecomapi.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil{
    private String SECRET_KEY = "bdisba54mlkdfmkls5nmsdklklsmc1427424425145x";

    public String accessToken(String username) {
        /*UserName means email address*/
        Map<String, Object> claims = new HashMap();
        return createToken(claims, username);
    }

    public String refreshToken(String username) {
        /*UserName means email address*/
        Map<String, Object> claims = new HashMap();
        return refreshToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().claims(claims).subject(subject).header().empty().add("typ", "JWT").and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 100 * 60 * 60))
                .signWith(getSigningKey()).compact();
    }

    private String refreshToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().claims(claims).subject(subject).header().empty().add("typ", "JWT").and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .signWith(getSigningKey()).compact();
    }


    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));
    }

    //  Utility method
    private Claims extractAllClaims(String rawToken) {
        String token = rawToken.trim();  // remove leading/trailing whitespace
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    //  Utility method
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    //  Utility method
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

}
