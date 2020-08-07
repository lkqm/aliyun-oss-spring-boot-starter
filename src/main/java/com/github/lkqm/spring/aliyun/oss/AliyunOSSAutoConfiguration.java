package com.github.lkqm.spring.aliyun.oss;

import com.github.lkqm.spring.aliyun.oss.template.AliyunOSSTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS自动配置类
 */
@Configuration
public class AliyunOSSAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "aliyun.oss")
    public AliyunOSSConfig aliyunOSSConfig() {
        return new AliyunOSSConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public AliyunOSSTemplate aliyunOSSTemplate(AliyunOSSConfig config) {
        return new AliyunOSSTemplate(config);
    }

}
