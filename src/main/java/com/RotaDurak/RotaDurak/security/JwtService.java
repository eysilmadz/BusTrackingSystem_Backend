package com.RotaDurak.RotaDurak.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class JwtService {
    private static final String KEY = "123456";

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+ 86400000)) //1 gün geçerli
                .signWith(SignatureAlgorithm.HS256, KEY)
                .compact();
    }

    public String extractEmail(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
