package com.github.lkqm.spring.aliyun.oss.template;

import com.aliyun.oss.OSS;
import java.io.InputStream;

public interface AliyunOSSOptions {

    //------------------------------------------------------------------
    // 基本
    //------------------------------------------------------------------

    OSS createOSSClient();

    OSS getOSSClient();

    //------------------------------------------------------------------
    // 上传文件
    //------------------------------------------------------------------

    String uploadFileText(String text, String pathKey);

    String uploadFileText(String text, String pathKey, String bucket);

    String uploadBase64Image(String base64, String pathKey);

    String uploadBase64Image(String base64, String pathKey, String bucket);

    String uploadFileBytes(byte[] bytes, String pathKey);

    String uploadFileBytes(byte[] bytes, String pathKey, String bucket);

    String uploadFileStream(InputStream stream, String pathKey);

    String uploadFileStream(InputStream stream, String pathKey, String bucket);

    //------------------------------------------------------------------
    // 辅助方法
    //------------------------------------------------------------------

    String generateUrl(String pathKey);

    String generateUrl(String pathKey, String bucket);

    String generatePathKey(String url);

    String generateHost(String endpoint, String bucket);

}
