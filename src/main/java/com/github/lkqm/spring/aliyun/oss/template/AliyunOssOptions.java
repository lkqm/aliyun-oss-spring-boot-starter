package com.github.lkqm.spring.aliyun.oss.template;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import java.io.File;
import java.io.InputStream;

/**
 * 提供简化上传/下载的基本操作，复杂操作请使用原始客户端OSS.
 * <p>
 * 上传文件: uploadXxx 下载文件: downloadXxx
 */
public interface AliyunOssOptions {

    //------------------------------------------------------------------
    // 基本
    //------------------------------------------------------------------

    OSS getOssClient();

    SecurityTokenResult generateSecurityToken(String sessionName, String roleArn, long durationSeconds);

    SecurityTokenResult generateSecurityToken(String bucket, String sessionName, String roleArn, long durationSeconds);

    PostPolicyResult generateClientPolicy(String pathKey, int expireSeconds, long minSize, long maxSize);

    PostPolicyResult generateClientPolicy(String bucket, String pathKey, int expireSeconds, long minSize, long maxSize);

    //------------------------------------------------------------------
    // 上传文件
    //------------------------------------------------------------------

    String uploadFileText(String pathKey, String text);

    String uploadFileText(String bucket, String pathKey, String text);

    String uploadFileBase64Image(String pathKey, String base64);

    String uploadFileBase64Image(String bucket, String pathKey, String base64);

    String uploadFileBytes(String pathKey, byte[] bytes);

    String uploadFileBytes(String bucket, String pathKey, byte[] bytes);

    String uploadFileStream(String pathKey, InputStream stream);

    String uploadFileStream(String bucket, String pathKey, InputStream stream);

    //------------------------------------------------------------------
    // 下载文件
    //------------------------------------------------------------------

    File downloadFileTmp(String pathKey);

    File downloadFileTmp(String bucket, String pathKey);

    ObjectMetadata downloadFile(String pathKey, String file);

    ObjectMetadata downloadFile(String bucket, String pathKey, String file);

    ObjectMetadata downloadFile(String pathKey, Consumer<InputStream> handler);

    ObjectMetadata downloadFile(String bucket, String pathKey, Consumer<InputStream> handler);

    //------------------------------------------------------------------
    // 辅助方法
    //------------------------------------------------------------------

    String calculateUrl(String pathKey);

    String calculateUrl(String bucket, String pathKey);

    String calculatePathKey(String url);

    String calculateHost(String endpoint, String bucket);

}
