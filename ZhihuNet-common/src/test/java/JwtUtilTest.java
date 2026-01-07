import org.acpasser.zhihunet.common.utils.JwtUtil;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class JwtUtilTest {

    private static final String USER_ID = "12345";
    private static final String USERNAME = "testuser";

    @Test
    void testGenerateAndParseToken_Success() {
        // 准备测试数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", USER_ID);
        claims.put("username", USERNAME);
        claims.put("role", "admin");

        // 执行：生成token
        String token = JwtUtil.genJwtToken(claims);
        assertEquals(3, token.split("\\.").length, "JWT token应该包含3部分");

        // 执行：解析token
        Map<String, Object> parsedClaims = JwtUtil.parseToken(token);

        // 验证
        assertNotNull(parsedClaims, "解析的claims不应为null");
        assertEquals(USER_ID, parsedClaims.get("userId"), "userId应该匹配");
        assertEquals(USERNAME, parsedClaims.get("username"), "username应该匹配");
        assertEquals("admin", parsedClaims.get("role"), "role应该匹配");
    }

    @Test
    void testGenerateAndParseToken_WithBearerPrefix() {
        // 准备测试数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", USER_ID);

        // 生成token
        String token = JwtUtil.genJwtToken(claims);

        // 测试带Bearer前缀的token
        String bearerToken = "Bearer " + token;
        Map<String, Object> parsedClaims = JwtUtil.parseToken(bearerToken);

        // 验证
        assertNotNull(parsedClaims);
        assertEquals(USER_ID, parsedClaims.get("userId"));
    }

    @Test
    void testGenerateToken_WithCustomExpireTime() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("test", "value");

        // 生成1分钟过期的token
        String token = JwtUtil.genJwtToken(claims, 1000L * 60);

        assertNotNull(token);

        // 验证token可以正常解析
        assertDoesNotThrow(() -> JwtUtil.parseToken(token));
    }

    @Test
    void testValidateToken_ValidToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", USER_ID);

        String token = JwtUtil.genJwtToken(claims);

        assertTrue(JwtUtil.validateToken(token), "有效token应该验证通过");
        assertTrue(JwtUtil.validateToken("Bearer " + token), "带Bearer前缀的有效token应该验证通过");
    }

    @Test
    void testValidateToken_InvalidToken() {
        assertFalse(JwtUtil.validateToken("invalid.token.string"), "无效token应该验证失败");
        assertFalse(JwtUtil.validateToken("Bearer invalid"), "无效的Bearer token应该验证失败");

        // 空
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> JwtUtil.validateToken(""));
        assertEquals("Token cannot be null or empty", exception.getMessage());

        // null
        exception = assertThrows(
                IllegalArgumentException.class,
                () -> JwtUtil.validateToken(null));
        assertEquals("Token cannot be null or empty", exception.getMessage());
    }

    @Test
    void testParseToken_ExpiredToken() throws InterruptedException {
        // 生成1毫秒过期的token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", USER_ID);

        String token = JwtUtil.genJwtToken(claims, 1L); // 1毫秒后过期

        // 等待token过期
        TimeUnit.MILLISECONDS.sleep(10);

        // 验证过期token抛出异常
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> JwtUtil.parseToken(token));

        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    void testParseToken_NullOrEmptyToken() {
        assertThrows(IllegalArgumentException.class,
                () -> JwtUtil.parseToken(null));

        assertThrows(IllegalArgumentException.class,
                () -> JwtUtil.parseToken(""));

        assertThrows(IllegalArgumentException.class,
                () -> JwtUtil.parseToken("   "));
    }


    @Test
    void testGetExpirationDate() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", USER_ID);

        String token = JwtUtil.genJwtToken(claims);

        assertDoesNotThrow(() -> {
            Date expiration = JwtUtil.getExpirationDate(token);
            assertNotNull(expiration);
            assertTrue(expiration.after(new Date()), "过期时间应该在当前时间之后");
        });
    }

    @Test
    void testGetExpirationDate_InvalidToken() {
        assertThrows(SecurityException.class,
                () -> JwtUtil.getExpirationDate("invalid.token"));
    }


    @Test
    void testConcurrentTokenGeneration() throws InterruptedException {
        // 测试多线程下的线程安全性
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                Map<String, Object> claims = new HashMap<>();
                claims.put("threadIndex", index);
                claims.put("userId", "user_" + index);

                String token = JwtUtil.genJwtToken(claims);
                assertNotNull(token);

                Map<String, Object> parsedClaims = JwtUtil.parseToken(token);
                assertEquals(index, parsedClaims.get("threadIndex"));
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
    }
}