package org.acpasser.zhihunet.crawler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootApplication
@ComponentScan({
        "org.acpasser.zhihunet.crawler",  // crawler 模块的服务类（如 CrawlerRpcServiceImpl）
        "org.acpasser.zhihunet.repository" // repo 模块的仓库类（如 ZhihuUserRepository）
})
// 扫描所有 MyBatis Mapper 接口（确保覆盖 mapper 和 repository 子包）
@MapperScan({
        "org.acpasser.zhihunet.repository.mybatis.mapper",    // UserMapper 所在包
        "org.acpasser.zhihunet.repository.mybatis.repository" // ZhihuUserRepository 所在包
})
public class ZhihuNetCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhihuNetCrawlerApplication.class, args);
    }

    // 统一 JSON 处理规则
    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        builder.failOnUnknownProperties(false);
        builder.defaultViewInclusion(true);
        return builder;
    }
}
