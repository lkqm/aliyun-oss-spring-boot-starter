package com.github.lkqm.spring.aliyun.oss;

import static com.github.lkqm.spring.aliyun.oss.template.InnerUtils.checkArgument;

import com.github.lkqm.spring.aliyun.oss.template.InnerUtils;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * 阿里云OSS配置属性
 */
@Data
public class AliyunOssProperties implements Serializable, InitializingBean {

    public static final String PREFIX = "aliyun.oss";

    /**
     * 服务地址
     */
    private String endpoint;

    /**
     * 服务地址(内网)
     */
    private String internalEndpoint;

    /**
     * 区域id
     */
    private String regionId;

    /**
     * 访问标识
     */
    private String accessKeyId;

    /**
     * 访问密钥
     */
    private String accessKeySecret;

    /**
     * 会话token
     */
    private String securityToken;

    /**
     * 授权信息
     */
    private String roleArn;

    /**
     * 自定义域名, bucket名称忽略大小写
     */
    private Map<String, String> bucketCustomDomain;

    /**
     * Bucket
     */
    private String bucket;

    @Override
    public void afterPropertiesSet() {
        checkArgument(!StringUtils.isEmpty(endpoint), "'endpoint' can't be empty");
        checkArgument(!StringUtils.isEmpty(regionId), "'regionId' can't be empty");
        checkArgument(!StringUtils.isEmpty(accessKeyId), "'accessKeyId' can't be empty");
        checkArgument(!StringUtils.isEmpty(accessKeySecret), "'accessKeySecret' can't be empty");
        checkArgument(!StringUtils.isEmpty(bucket), "'bucket' can't be empty");
    }

    /**
     * 获取请求到阿里云的服务地址, 内网地址优先
     */
    public String getRequestEndpoint() {
        if (this.internalEndpoint != null && this.internalEndpoint.length() > 0) {
            return internalEndpoint;
        }
        return endpoint;
    }

    /**
     * 获取访问访问前缀, 优先级: customDomain > endpoint
     */
    public String getHostByBucket(String bucket) {
        if (bucketCustomDomain != null) {
            String domain = bucketCustomDomain.get(bucket);
            if (domain != null && domain.length() > 0) {
                return domain;
            }
        }
        return InnerUtils.generateHost(this.endpoint, bucket);
    }
}