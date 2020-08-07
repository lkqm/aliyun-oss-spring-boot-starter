package com.github.lkqm.spring.aliyun.oss.template;

import java.io.Serializable;
import lombok.Data;

/**
 * 客户端获取上传Policy加密结果
 */
@Data
public class PostPolicyResult implements Serializable {

    /**
     * 服务端点
     */
    private String endpoint;

    /**
     * 上传的bucket
     */
    private String bucket;

    /**
     * 主机地址, 用于拼接URL
     */
    private String host;

    /**
     * 过期时间
     */
    private Long expireAt;

    private String accessKeyId;

    private String policy;

    private String signature;

    private String key;


}