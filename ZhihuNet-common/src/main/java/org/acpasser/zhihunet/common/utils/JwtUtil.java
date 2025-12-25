package org.acpasser.zhihunet.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.constant.Constant;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class JwtUtil {

    private static final String KEY = "org.acpasser.zhihunet.crawler.zhihu-net";
    private static final Integer DEFAULT_EXPIRE_TIME = 1000 * 60 * 60 * Constant.REDIS_USER_KEY_EXPIRATION_HOUR;
    private static final String CLAIMS_KEY = "data";
    private static final Algorithm algorithm = Algorithm.HMAC256(KEY);
    private static final JWTVerifier VERIFIER = JWT.require(algorithm).build();


    /**
     * 生成JWT Token
     */
    public static String genJwtToken(Map<String, Object> claims) {
        return genJwtToken(claims, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 生成JWT Token（自定义过期时间）
     */
    public static String genJwtToken(Map<String, Object> claims, long expireTimeMills) {
        try {
            return JWT.create()
                    .withClaim(CLAIMS_KEY, claims)    // 注意载荷不能存放私密信息
                    .withExpiresAt(new Date(System.currentTimeMillis() + expireTimeMills))
                    .withJWTId(UUID.randomUUID().toString())
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            log.error("JWT token generation failed", e);
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    /**
     * 解析验证Token
     */
    public static Map<String, Object> parseToken(String token) {
        String actualToken = getActualToken(token);
        try {
            DecodedJWT jwt = VERIFIER.verify(actualToken);
            return jwt.getClaim(CLAIMS_KEY).asMap();
        } catch (AlgorithmMismatchException e) {
            log.error("JWT algorithm mismatch", e);
            throw new SecurityException("Token algorithm mismatch");
        } catch (SignatureVerificationException e) {
            log.error("JWT signature verification failed", e);
            throw new SecurityException("Invalid token signature");
        } catch (TokenExpiredException e) {
            log.warn("JWT token expired");
            throw new SecurityException("Token expired");
        } catch (InvalidClaimException e) {
            log.error("JWT invalid claim", e);
            throw new SecurityException("Invalid token claims");
        } catch (JWTDecodeException e) {
            log.error("JWT decode failed", e);
            throw new IllegalArgumentException("Invalid token format");
        } catch (JWTVerificationException e) {
            log.error("JWT verification failed", e);
            throw new SecurityException("Token verification failed");
        }
    }

    /**
     * 验证Token是否有效（不解析内容）
     */
    public static boolean validateToken(String token) {
        String actualToken = getActualToken(token);
        try {
            VERIFIER.verify(actualToken);
            return true;
        } catch (JWTVerificationException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取Token的过期时间
     */
    public static Date getExpirationDate(String token) {
        String actualToken = getActualToken(token);
        try {
            DecodedJWT jwt = VERIFIER.verify(actualToken);
            return jwt.getExpiresAt();
        } catch (JWTVerificationException e) {
            log.error("get expiration date failed: {}", e.getMessage());
            throw new SecurityException("Cannot get expiration date from invalid token", e);
        }
    }

    private static String getActualToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }
}
