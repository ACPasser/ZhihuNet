package org.acpasser.zhihunet.console.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 处理静态资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${avatar.upload-path}")
    private String avatarUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 头像资源映射
        registry.addResourceHandler("/avatars/**")
            .addResourceLocations("file:" + avatarUploadPath)
            .setCachePeriod(86400);
    }
}