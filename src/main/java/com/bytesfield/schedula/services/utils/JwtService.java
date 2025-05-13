package com.bytesfield.schedula.services.utils;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JwtService provides utility methods for generating, validating, and managing JWT tokens.
 * It supports access and refresh tokens, token invalidation, and caching.
 */
@Slf4j
@Service
public class JwtService {

    private static final String INVALIDATED_VALUE = "invalidated";
    private final CacheService cacheService;

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${security.jwt.refresh-token.secret-key}")
    private String refreshTokenSecretKey;

    @Value("${security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    /**
     * Constructor for JwtService.
     *
     * @param cacheService the CacheService used for caching tokens
     */
    public JwtService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Generates access and refresh tokens for a given email.
     *
     * @param email the email for which tokens are generated
     * @return a map containing the access and refresh tokens
     */
    public Map<String, Object> generateTokens(String email) {
        String accessToken = buildToken(email, JwtTokenType.ACCESS, jwtExpiration);
        String refreshToken = buildToken(email, JwtTokenType.REFRESH, refreshTokenExpiration);

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    /**
     * Builds a JWT token with the specified parameters.
     *
     * @param email      the email to include in the token
     * @param type       the type of the token (ACCESS or REFRESH)
     * @param expiration the expiration time in seconds
     * @return the generated JWT token
     * @throws IllegalArgumentException if the expiration time is not positive
     */
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

    /**
     * Retrieves the signing key for the specified token type.
     *
     * @param type the type of the token (ACCESS or REFRESH)
     * @return the signing key
     */
    private Key getSignInKey(JwtTokenType type) {
        String key = type == JwtTokenType.ACCESS ? secretKey : refreshTokenSecretKey;
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validates a token by checking its username and expiration.
     *
     * @param token       the token to validate
     * @param type        the type of the token (ACCESS or REFRESH)
     * @param userDetails the user details to validate against
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, JwtTokenType type, UserDetails userDetails) {
        final String username = extractUsername(token, type);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, type);
    }

    /**
     * Extracts the username from a token.
     *
     * @param token the token to extract the username from
     * @param type  the type of the token (ACCESS or REFRESH)
     * @return the username
     */
    public String extractUsername(String token, JwtTokenType type) {
        return extractAllClaims(token, type).getSubject();
    }

    /**
     * Checks if a token is expired.
     *
     * @param token the token to check
     * @param type  the type of the token (ACCESS or REFRESH)
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token, JwtTokenType type) {
        return extractExpiration(token, type).before(new Date());
    }

    /**
     * Invalidates an access token by storing its JTI in the cache with a TTL.
     *
     * @param token the token to invalidate
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
                cacheService.setValueWithExpiration(jti, INVALIDATED_VALUE, ttlMillis, TimeUnit.MILLISECONDS);
            }
        } catch (ExpiredJwtException ex) {
            log.error("Token already expired: {}", ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid token: {}", ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Error invalidating token: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Extracts all claims from a token.
     *
     * @param token the token to extract claims from
     * @param type  the type of the token (ACCESS or REFRESH)
     * @return the claims
     */
    private Claims extractAllClaims(String token, JwtTokenType type) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey(type))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if an access token's JTI is marked as invalidated in the cache.
     *
     * @param jti the JWT ID
     * @return true if the token is invalidated, false otherwise
     */
    public boolean isAccessTokenInvalidated(String jti) {
        if (jti == null || jti.trim().isEmpty()) return false;
        return cacheService.isKeyExists(jti);
    }

    /**
     * Removes an access token's invalidation from the cache.
     *
     * @param token the token to remove invalidation for
     */
    public void removeAccessTokenInvalidation(String token) {
        try {
            String jti = extractJwtId(token, JwtTokenType.ACCESS);
            if (jti != null) {
                cacheService.deleteValue(jti);
            }
        } catch (Exception ex) {
            log.error("Error removing invalidation: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Extracts the JWT ID (JTI) from a token.
     *
     * @param token the token to extract the JTI from
     * @param type  the type of the token (ACCESS or REFRESH)
     * @return the JTI
     */
    public String extractJwtId(String token, JwtTokenType type) {
        return extractAllClaims(token, type).getSubject();
    }

    /**
     * Caches an access token for a user with an expiration time.
     *
     * @param user  the user associated with the token
     * @param token the token to cache
     */
    public void cacheAccessToken(User user, String token) {
        Date expirationDate = this.extractExpiration(token, JwtTokenType.ACCESS);
        String encryptedToken = EncryptionUtil.encrypt(token);
        cacheService.setValueWithExpiration(this.getJwtAccessTokenKey(user.getEmail()), encryptedToken, expirationDate.getTime(), TimeUnit.MILLISECONDS);
    }

    /**
     * Extracts the expiration date from a token.
     *
     * @param token the token to extract the expiration date from
     * @param type  the type of the token (ACCESS or REFRESH)
     * @return the expiration date
     */
    public Date extractExpiration(String token, JwtTokenType type) {
        return extractAllClaims(token, type).getExpiration();
    }

    /**
     * Constructs the cache key for a user's JWT access token.
     *
     * @param email the user's email
     * @return the cache key
     */
    public String getJwtAccessTokenKey(String email) {
        return "jwt:" + email;
    }

    /**
     * Retrieves a cached access token for a user.
     *
     * @param identifier the user's identifier (e.g., email)
     * @return the decrypted access token
     */
    public String getCachedAccessToken(String identifier) {
        String token = cacheService.getValue(this.getJwtAccessTokenKey(identifier));
        return EncryptionUtil.decrypt(token);
    }

    /**
     * Deletes a cached access token for a user.
     *
     * @param identifier the user's identifier (e.g., email)
     */
    public void deleteCachedAccessToken(String identifier) {
        cacheService.deleteValue(this.getJwtAccessTokenKey(identifier));
    }
}