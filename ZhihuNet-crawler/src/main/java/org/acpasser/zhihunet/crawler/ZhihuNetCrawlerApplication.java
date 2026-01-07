package org.acpasser.zhihunet.crawler;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

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

}
