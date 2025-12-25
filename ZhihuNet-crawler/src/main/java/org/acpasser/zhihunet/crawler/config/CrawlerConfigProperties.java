package org.acpasser.zhihunet.crawler.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "crawler")
@Validated
@Data
@Slf4j
public class CrawlerConfigProperties {
    // 1. SSL 信任配置：非必填，默认 false
    private boolean trustAllSsl = false;

    // 2. 连接池配置：必须为正数（至少1，否则连接池无效）
    @Min(value = 1, message = "最大总连接数必须大于0")
    private int maxConnTotal = 100;
    @Min(value = 1, message = "单路由最大连接数必须大于0")
    private int maxConnPerRoute = 20;
    @Min(value = 30, message = "连接存活时间不能小于30秒（避免频繁创建连接）")
    private int connectionTimeToLiveSeconds = 60;
    @Min(value = 1, message = "连接池等待超时不能小于1秒（过小会频繁超时）")
    private int connectionRequestTimeoutSeconds = 5;

    // 3. 超时配置
    @Min(value = 1, message = "连接超时不能小于1s")
    private int connectTimeoutSeconds = 5;
    @Min(value = 1, message = "响应超时不能小于1s")
    private int responseTimeoutSeconds = 5;

    // 4. 认证信息
    @NotBlank(message = "Cookie不能为空")
    private String cookie;
    @NotBlank(message = "userAgent不能为空")
    private String userAgent;
    @NotBlank(message = "d_c0不能为空")
    private String dC0;
    @NotBlank(message = "x-zse-93不能为空")
    private String xZse93;

    // 5. 爬取控制参数
    @Min(value = 0, message = "最大重试次数不能小于0")
    @Max(value = 3, message = "最大重试次数不能大于3（建议0-3次）")
    private int maxRetries = 0;
    // 重试间隔（随机范围，单位：秒）
    @Min(value = 1, message = "最小重试间隔不能小于1秒")
    private int minRetryDelaySeconds = 1;
    @Min(value = 1, message = "最大重试间隔不能小于1秒")
    private int maxRetryDelaySeconds = 10;
    @Min(value = 0, message = "0=无限等待，非0=按配置超时等待")
    private int crawlAwaitMinutes = 1;
    @Min(value = 1, message = "爬取重试等待超时不能小于1分钟")
    private int crawlRetryAwaitMinutes = 1;
    // 并发线程数（根据目标网站反爬强度调整）
    @Min(value = 1, message = "线程数不能小于1")
    @Max(value = 8, message = "线程数不能大于8（避免反爬/资源耗尽）")
    private int threadCount = 4;
    // 批量入库阈值（达到该数量则触发一次批量插入）
    @Min(value = 1, message = "批量大小不能小于1")
    @Max(value = 1000, message = "批量大小不能大于1000（避免OOM）")
    private int batchSize = 100;
    // zse96参数解析脚本超时时间
    @Min(value = 1, message = "脚本超时时间不能小于1秒")
    private int scriptTimeoutSeconds = 3;

    @PostConstruct
    public void printConfig() {
        log.info("CrawlerConfig 加载结果：{}", this);
    }
}
