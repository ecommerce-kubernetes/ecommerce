package com.example.order_service.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class JwtValidator {
    private final Key key;

    public JwtValidator(@Value("token.secret") String keyString){
        byte[] keyBytes = Decoders.BASE64.decode(keyString);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String getSub(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
