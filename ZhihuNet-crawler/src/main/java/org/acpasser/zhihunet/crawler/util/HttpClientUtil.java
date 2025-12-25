package org.acpasser.zhihunet.crawler.util;

import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.exception.BusinessException;
import org.acpasser.zhihunet.crawler.config.CrawlerConfigProperties;
import org.acpasser.zhihunet.crawler.request.ZhihuRequest;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

@Component
@Slf4j
public class HttpClientUtil {

    @Autowired
    private CrawlerConfigProperties config;

    @Autowired
    private CloseableHttpClient httpClient;

    private final Random random = new Random();

    /**
     * 通用GET请求方法（带重试、异常处理）
     * @param request 请求参数
     * @return Jsoup Document（解析后的HTML文档）
     */
    public Document doGetWithRetry(ZhihuRequest request) {
        String url = request.getUrl();
        URI uri;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            Map<String, String> params = request.getParams();
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    uriBuilder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            log.error("URL构建失败，原始URL: {}, 参数: {}", url, request.getParams(), e);
            throw new IllegalArgumentException("请求URL格式错误，请检查URL和参数", e);
        }

        // 创建HttpGet对象
        HttpGet httpGet = new HttpGet(uri);

        int attempts = 0;
        int maxRetries = this.config.getMaxRetries();
        while (attempts < maxRetries) {
            // 1. try-with-resources（自动关闭响应，释放连接）
            // 反爬：随机休眠
            pauseForRandomTime();
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getCode() != HttpStatus.SC_OK) {
                    EntityUtils.consume(response.getEntity()); // 释放资源
                    log.error("Get请求失败，URL: {}，Response: {}，请检查Cookie", url, response);
                    throw new BusinessException(String.format("Get请求失败，URL: %s，Response: %s，请检查Cookie", url, response));
                }
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new BusinessException("响应实体为空，URL: " + url);
                }
                String responseBody;
                try {
                    // 1. 默认使用 UTF-8 字符集（中文网站最常用）
                    responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                } catch (ParseException e1) {
                    log.error("UTF-8 解析失败，URL: {}，尝试 GBK 编码...", url);
                    try {
                        // 2. 尝试 GBK（部分旧中文网站使用）
                        responseBody = EntityUtils.toString(entity, Charset.forName("GBK"));
                    } catch (ParseException e2) {
                        log.error("GBK 解析失败，URL: {}，尝试 ISO-8859-1 编码...", url);
                        try {
                            // 3. 尝试：ISO-8859-1（西方字符集，兼容部分乱码场景）
                            responseBody = EntityUtils.toString(entity, StandardCharsets.ISO_8859_1);
                        } catch (ParseException e3) {
                            // 终止解析
                            log.error("ISO-8859-1 解析失败，URL: {}，终止解析！", url);
                            throw new BusinessException(
                                    String.format("三种编码方式均解析失败（UTF-8/GBK/ISO-8859-1），URL: %s", url),
                                    e3 // 保留原始异常栈，便于定位问题
                            );
                        }
                    }
                }
                EntityUtils.consume(entity);
                // debug用
                // log.info(responseBody);
                return Jsoup.parse(responseBody);
            } catch (SSLException e) { // SSL异常（重试场景）
                attempts++;
                if (attempts >= maxRetries) {
                    log.error("SSL异常: 尝试{}次后仍无法连接，URL: {}，终止解析！", maxRetries, url, e);
                    throw new BusinessException(String.format("SSL异常: 尝试%d次后仍无法连接，URL: %s，终止解析！",
                            maxRetries, url), e);
                }
                log.warn("SSL异常，URL: {}，重试({}/{})...", url, attempts, maxRetries);
                pauseForRandomTime();
            } catch (IOException e) { // 其他IO异常（直接抛出，由调用方处理）
                throw new BusinessException(String.format("请求失败，URL: %s", url), e);
            }
        }
        return null;
    }

    /**
     * 随机暂停（重试间隔，避免固定间隔触发反爬）
     */
    public void pauseForRandomTime() {
        try {
            int delay = random.nextInt(config.getMaxRetryDelaySeconds() - config.getMinRetryDelaySeconds())
                    + config.getMinRetryDelaySeconds();
            log.info("为防止IP被禁, 随机睡眠{}秒...", delay);
            Thread.sleep(delay * 1000L);
        } catch (InterruptedException e) {
            log.warn("线程在睡眠过程中被中断", e);
            Thread.currentThread().interrupt(); // 恢复中断状态
        }
    }
}