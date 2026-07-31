    package com.zjsru.utils;

    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.JwtException;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.SignatureAlgorithm;
    import io.jsonwebtoken.security.Keys;
    import lombok.Data;
    import org.springframework.stereotype.Component;

    import javax.crypto.SecretKey;
    import java.nio.charset.StandardCharsets;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Map;

    @Data
    @Component
    public class JwtUtil {

        // 1. 密钥
        private static final String SECRET_STRING = "zjsru_web_development_course_project_secret_key_2025";

        // 将字符串密钥转换为强加密 Key 对象
        private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

        // 2. 过期时间
        private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 100;

        public static String generateToken(Integer userId, String username) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", username);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userId.toString())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(KEY, SignatureAlgorithm.HS256)
                    .compact();
        }

        public static Integer getUserIdFromToken(String token) {
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Token为空");
            }

            // 处理 "Bearer " 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(KEY)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                return Integer.parseInt(claims.getSubject());
            } catch (JwtException e) {
                // Token 过期
                throw new RuntimeException("Token无效或已过期");
            }
        }

        public static String getUsernameFromToken(String token) {
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Token为空");
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(KEY)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                return claims.get("username", String.class);
            } catch (Exception e) {
                throw new RuntimeException("Token无效");
            }
        }
        public static boolean validateToken(String token, String username) {
            try {
                String tokenUsername = getUsernameFromToken(token);
                return username.equals(tokenUsername) && !isTokenExpired(token);
            } catch (Exception e) {
                return false;
            }
        }

        private static boolean isTokenExpired(String token) {
            try {
                Date expiration = getExpirationDateFromToken(token);
                return expiration.before(new Date());
            } catch (Exception e) {
                return true;
            }
        }

        private static Date getExpirationDateFromToken(String token) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        }

    }