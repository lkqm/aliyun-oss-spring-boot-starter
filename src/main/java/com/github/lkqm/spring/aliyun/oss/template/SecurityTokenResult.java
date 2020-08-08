package com.github.lkqm.spring.aliyun.oss.template;

import java.io.Serializable;
import lombok.Data;

/**
 * STS临时授权信息
 */
@Data
public class SecurityTokenResult implements Serializable {

    /**
     * 服务端点
     */
    private String endpoint;

    /**
     * 上传的bucket
     */
    private String bucket;

    /**
     * 主机地址, 用于拼接URL, 自定义域名优先
     */
    private String host;

    /**
     * 访问标识
     */
    private String accessKeyId;

    /**
     * 访问密钥
     */
    private String accessKeySecret;

    /**
     * 访问Token
     */
    private String securityToken;

    /**
     * 过期时间
     */
    private Long expireAt;

}
