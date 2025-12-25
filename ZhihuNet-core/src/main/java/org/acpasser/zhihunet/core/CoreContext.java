package org.acpasser.zhihunet.core;


import org.acpasser.zhihunet.repository.RepositoryContext;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("org.acpasser.zhihunet.core")
@MapperScan("org.acpasser.zhihunet.repository.mybatis.mapper")
@Import(value = { RepositoryContext.class })
@EnableDubbo(scanBasePackages = "org.acpasser.zhihunet.core")
public class CoreContext {
}
