package com.bytesfield.schedula.services;

import com.bytesfield.schedula.models.entities.User;
import com.bytesfield.schedula.models.enums.JwtTokenType;
import com.bytesfield.schedula.utils.Helper;
import com.bytesfield.schedula.utils.security.EncryptionUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JwtService {

    private static final String INVALIDATED_VALUE = "invalidated";
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${security.jwt.refresh-token.secret-key}")
    private String refreshTokenSecretKey;
    @Value("${security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    public JwtService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Map<String, Object> generateTokens(String email) {
        String accessToken = buildToken(email, JwtTokenType.ACCESS, jwtExpiration);
        String refreshToken = buildToken(email, JwtTokenType.REFRESH, refreshTokenExpiration);

        Map<String, Object> tokens = new HashMap<>();

        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    private String buildToken(String email, JwtTokenType type, long expiration) {
        if (expiration <= 0) {
            throw new IllegalArgumentException("Expiration time must be positive");
        }

        long nowMilliseconds = System.currentTimeMillis();
        Date now = new Date(nowMilliseconds);

        Date expiryDate = new Date(nowMilliseconds + Helper.secondsToMilliseconds(expiration));

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignInKey(type), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey(JwtTokenType type) {
        String key = type == JwtTokenType.ACCESS ? secretKey : refreshTokenSecretKey;

        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, JwtTokenType type, UserDetails userDetails) {
        final String username = extractUsername(token, type);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, type);
    }

    public String extractUsername(String token, JwtTokenType type) {
        return extractAllClaims(token, type).getSubject();
    }

    public boolean isTokenExpired(String token, JwtTokenType type) {
        return extractExpiration(token, type).before(new Date());
    }

    /**
     * Invalidates a JWT by storing its JTI in Redis with the appropriate TTL.
     *
     * @param token The JWT to invalidate
     */
    public void invalidateAccessToken(String token) {
        if (token == null || token.trim().isEmpty()) return;

        try {
            Claims claims = this.extractAllClaims(token, JwtTokenType.ACCESS);
            String jti = claims.getSubject();
            Date expiration = claims.getExpiration();

            if (jti == null || expiration == null) {
                log.error("Invalid token: missing JTI or expiration");
                return;
            }

            long ttlMillis = expiration.getTime() - System.currentTimeMillis();

            if (ttlMillis > 0) {
                redisTemplate.opsForValue().set(jti, INVALIDATED_VALUE, ttlMillis, TimeUnit.MILLISECONDS);
            }
        } catch (ExpiredJwtException ex) {
            log.error("Token already expired: {}", ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid token: {}", ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Error invalidating token: {}", ex.getMessage(), ex);
        }

    }

    private Claims extractAllClaims(String token, JwtTokenType type) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey(type))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if a token's JTI is marked invalidated in Redis.
     *
     * @param jti The JWT ID
     * @return true if invalidated, false otherwise
     */
    public boolean isAccessTokenInvalidated(String jti) {
        if (jti == null || jti.trim().isEmpty()) return false;

        return redisTemplate.hasKey(jti);
    }

    public void removeAccessTokenInvalidation(String token) {
        try {
            String jti = extractJwtId(token, JwtTokenType.ACCESS);

            if (jti != null) {
                redisTemplate.opsForValue().getOperations().delete(jti);
            }
        } catch (Exception ex) {
            log.error("Error removing invalidation: {}", ex.getMessage(), ex);
        }
    }

    public String extractJwtId(String token, JwtTokenType type) {
        return extractAllClaims(token, type).getSubject();
    }

    public void cacheAccessToken(User user, String token) {
        Date expirationDate = this.extractExpiration(token, JwtTokenType.ACCESS);

        String encryptedToken = EncryptionUtil.encrypt(token);

        redisTemplate.opsForValue().set(this.getJwtAccessTokenKey(user.getEmail()), encryptedToken, expirationDate.getTime(), TimeUnit.MILLISECONDS);
    }

    public Date extractExpiration(String token, JwtTokenType type) {
        return extractAllClaims(token, type).getExpiration();
    }

    public String getJwtAccessTokenKey(String email) {
        return "jwt:" + email;
    }

    public String getCachedAccessToken(String identifier) {
        String token = redisTemplate.opsForValue().get(this.getJwtAccessTokenKey(identifier));

        return EncryptionUtil.decrypt(token);
    }

    public void deleteCachedAccessToken(String identifier) {
        redisTemplate.opsForValue().getAndDelete(this.getJwtAccessTokenKey(identifier));
    }
}
