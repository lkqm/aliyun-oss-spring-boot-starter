package com.github.lkqm.spring.aliyun.oss;

import static com.github.lkqm.spring.aliyun.oss.AliyunOssProperties.PREFIX;
import static com.github.lkqm.spring.aliyun.oss.template.InnerUtils.checkArgument;

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
import org.springframework.util.StringUtils;

/**
 * 阿里云OSS自动配置类
 */
@Configuration
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class AliyunOssAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OSS aliyunOssClient(AliyunOssProperties properties, CredentialsProvider credentialsProvider) {
        return new OSSClientBuilder().build(properties.getEndpoint(), credentialsProvider, properties.getConfig());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(PREFIX)
    public AliyunOssProperties aliyunOssProperties() {
        return new AliyunOssProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public CredentialsProvider aliyunOssCredentialsProvider(AliyunOssProperties properties) throws ClientException {
        checkArgument(!StringUtils.isEmpty(properties.getAccessKeyId()), "'accessKeyId' can't be empty");
        checkArgument(!StringUtils.isEmpty(properties.getAccessKeySecret()), "'accessKeySecret' can't be empty");
        if (!StringUtils.isEmpty(properties.getRoleArn())) {
            checkArgument(!StringUtils.isEmpty(properties.getRegionId()), "'regionId' can't be empty");
        }

        String requestEndpoint = properties.getRequestEndpoint();
        if (!StringUtils.isEmpty(properties.getRoleArn())) {
            DefaultProfile.addEndpoint("", properties.getRegionId(), "OSS", requestEndpoint);
            AlibabaCloudCredentials cdl = new BasicSessionCredentials(properties.getAccessKeyId(),
                    properties.getAccessKeySecret(), properties.getSecurityToken());
            IClientProfile profile = DefaultProfile
                    .getProfile(properties.getRegionId(), properties.getAccessKeyId(), properties.getSecurityToken());
            return new STSAssumeRoleSessionCredentialsProvider(cdl, properties.getRoleArn(), profile);
        } else {
            return new DefaultCredentialProvider(properties.getAccessKeyId(),
                    properties.getAccessKeySecret(), properties.getSecurityToken());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public AliyunOssTemplate aliyunOssTemplate(OSS oss, AliyunOssProperties properties) {
        return new AliyunOssTemplate(oss, properties);
    }

}
