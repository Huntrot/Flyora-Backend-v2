package org.example.flyora_backend.Utils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.example.flyora_backend.model.Account;
import org.example.flyora_backend.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    
    @Value("${app.jwt.secret}")
    private String secret;

    private final AccountRepository accountRepository;

    // Tạo khóa từ secret string
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Account account) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 86400000); // Token có hiệu lực trong 1 ngày

        return Jwts.builder()
                .setSubject(account.getUsername())
                .claim("id", account.getId())
                .claim("role", account.getRole().getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generatePreAuthToken(Account account) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 5 * 60 * 1000); // 5 phút

        return Jwts.builder()
                .setSubject(account.getUsername()) 
                .claim("id", account.getId())
                .claim("role", "PRE_AUTH")          // role tạm
                .claim("email", account.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public Account getAccountFromToken(String token) {
        String username = getUsernameFromToken(token);
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();

        return claims.getSubject();
    }
}