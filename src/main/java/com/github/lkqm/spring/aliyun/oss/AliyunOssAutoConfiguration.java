package com.github.lkqm.spring.aliyun.oss;

import static com.github.lkqm.spring.aliyun.oss.AliyunOssProperties.PREFIX;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.STSAssumeRoleSessionCredentialsProvider;
import com.aliyuncs.auth.AlibabaCloudCredentials;
import com.aliyuncs.auth.BasicSessionCredentials;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.github.lkqm.spring.aliyun.oss.template.AliyunOssTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS自动配置类
 */
@Configuration
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class AliyunOssAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(PREFIX)
    public AliyunOssProperties aliyunOssProperties() {
        return new AliyunOssProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public OSS aliyunOssClient(AliyunOssProperties properties) throws ClientException {
        String requestEndpoint = properties.getRequestEndpoint();
        CredentialsProvider credentialProvider;

        if (properties.getRoleArn() != null && properties.getRoleArn().length() > 0) {
            DefaultProfile.addEndpoint("", properties.getRegionId(), "OSS", requestEndpoint);
            AlibabaCloudCredentials cdl = new BasicSessionCredentials(properties.getAccessKeyId(),
                    properties.getAccessKeySecret(), properties.getSecurityToken());
            IClientProfile profile = DefaultProfile
                    .getProfile(properties.getRegionId(), properties.getAccessKeyId(), properties.getSecurityToken());
            credentialProvider = new STSAssumeRoleSessionCredentialsProvider(cdl, properties.getRoleArn(), profile);
        } else {
            credentialProvider = new DefaultCredentialProvider(properties.getAccessKeyId(),
                    properties.getAccessKeySecret(), properties.getSecurityToken());
        }

        return new OSSClientBuilder().build(properties.getEndpoint(), credentialProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public AliyunOssTemplate aliyunOssTemplate(OSS oss, AliyunOssProperties properties) {
        return new AliyunOssTemplate(oss, properties);
    }

}
