package org.acpasser.zhihunet.crawler.config;


import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

    /**
     * 配置全局单例的 CloseableHttpClient
     * 复用连接池、SSL配置、超时参数（从 CrawlerConfigProperties 读取）
     */
    @Bean(destroyMethod = "close") // 容器销毁时自动关闭 HttpClient，释放资源
    public CloseableHttpClient httpClient(CrawlerConfigProperties config)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        // 1. 配置连接池（从配置类读取参数）
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(config.getMaxConnTotal()) // 总连接数
                .setMaxConnPerRoute(config.getMaxConnPerRoute()) // 单路由连接数
                .setConnectionTimeToLive(TimeValue.ofSeconds(config.getConnectionTimeToLiveSeconds())) // 连接存活时间
                .setSSLSocketFactory(buildSSLSocketFactory(config.isTrustAllSsl()))
                .build();

        // 2. 配置请求超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .setResponseTimeout(config.getResponseTimeoutSeconds(), TimeUnit.SECONDS)
                .setConnectionRequestTimeout(config.getConnectionRequestTimeoutSeconds(), TimeUnit.SECONDS)
                .build();

        // 暂时没用
        // 3. 配置 Cookie 存储（加载知乎 Cookie）
//        CookieStore cookieStore = new BasicCookieStore();
//        String[] cookieParts = config.getCookie().split("; ");
//        for (String part : cookieParts) {
//            // 跳过空字符串（若 Cookie 末尾有多余的 "; "，会产生空元素）
//            if (part.trim().isEmpty()) {
//                continue;
//            }
//            // 拆分键值对（最多拆1次，保留值中的 "="）
//            String[] keyValue = part.split("=", 2);
//            if (keyValue.length == 2) {
//                BasicClientCookie cookie = new BasicClientCookie(keyValue[0].trim(), keyValue[1].trim());
//                cookie.setDomain(".zhihu.com");
//                cookie.setPath("/");
//                cookie.setSecure(true); // 知乎用 HTTPS，启用 Secure 属性
//                cookie.setHttpOnly(true); // 模拟浏览器的 HttpOnly 特性
//                cookieStore.addCookie(cookie);
//            } else {
//                // 日志记录异常 Cookie（如格式错误的键值对）
//                System.err.println("无效的 Cookie 格式：" + part);
//            }
//        }

        // 4. 请求拦截器（添加全局 Header）
        HttpRequestInterceptor requestInterceptor = (request, entityDetails, context) -> {
            request.setHeader("User-Agent", config.getUserAgent());
            request.setHeader("Cookie", config.getCookie());
            request.setHeader("Referer", "https://www.zhihu.com/");
            request.setHeader("x-zse-93", config.getXZse93());
        };

        // 2. 构建 HttpClient 实例（全局单例）
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
//                .setDefaultCookieStore(cookieStore)
                .addRequestInterceptorLast(requestInterceptor)
                .build();
    }

    /**
     * 构建 SSL 连接工厂（根据配置决定是否信任所有证书）
     */
    private SSLConnectionSocketFactory buildSSLSocketFactory(boolean trustAllSsl)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        if (trustAllSsl) {
            // 信任所有证书（适合测试环境，生产环境慎用）
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                    .build();
            return new SSLConnectionSocketFactory(sslContext);
        } else {
            // 使用默认 SSL 上下文（验证证书，适合生产环境）
            return SSLConnectionSocketFactory.getSocketFactory();
        }
    }
}