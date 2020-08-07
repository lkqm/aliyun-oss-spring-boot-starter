package com.github.lkqm.spring.aliyun.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.github.lkqm.spring.aliyun.oss.template.InnerUtils;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/**
 * 阿里云OSS配置属性
 */
@Data
public class AliyunOSSConfig implements Serializable {

    /**
     * 服务地址
     */
    private String endpoint;

    /**
     * 服务地址(内网)
     */
    private String internalEndpoint;

    /**
     * 自定义域名, bucket名称忽略大小写
     */
    private Map<String, String> bucketCustomDomain;

    /**
     * Bucket
     */
    private String bucket;

    /**
     * 访问标识
     */
    private String accessKeyId;

    /**
     * 访问密钥
     */
    private String accessKeySecret;

    public OSS createOSSClient() {
        String requestEndpoint = getRequestEndpoint();
        return new OSSClientBuilder().build(
                requestEndpoint,
                this.accessKeyId,
                this.accessKeySecret
        );
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