package com.github.lkqm.spring.aliyun.oss.template;

import static com.github.lkqm.spring.aliyun.oss.template.InnerUtils.checkArgument;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.github.lkqm.spring.aliyun.oss.AliyunOSSConfig;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.util.Base64Utils;

/**
 * 阿里云操作模版类, 简化常见操作
 */
public class AliyunOSSTemplate implements AliyunOSSOptions {

    private final AliyunOSSConfig config;
    private volatile OSS ossClient;

    public AliyunOSSTemplate(AliyunOSSConfig config) {
        this.config = config;
    }

    @Override
    public OSS createOSSClient() {
        return config.createOSSClient();
    }

    @Override
    public OSS getOSSClient() {
        if (ossClient == null) {
            synchronized (this) {
                if (ossClient == null) {
                    ossClient = createOSSClient();
                }
            }
        }
        return ossClient;
    }

    @Override
    public String uploadFileText(String text, String pathKey) {
        return uploadFileText(text, pathKey, config.getBucket());
    }

    @Override
    public String uploadFileText(String text, String pathKey, String bucket) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return uploadFileBytes(bytes, pathKey, bucket);
    }

    @Override
    public String uploadBase64Image(String base64, String pathKey) {
        return uploadBase64Image(base64, pathKey, config.getBucket());
    }

    @Override
    public String uploadBase64Image(String base64, String pathKey, String bucket) {
        byte[] bytes = Base64Utils.decodeFromString(base64);
        return uploadFileBytes(bytes, pathKey, bucket);
    }

    @Override
    public String uploadFileBytes(byte[] bytes, String pathKey) {
        return uploadFileBytes(bytes, pathKey, config.getBucket());
    }

    @Override
    public String uploadFileBytes(byte[] bytes, String pathKey, String bucket) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        return uploadFileStream(stream, pathKey, bucket);
    }

    @Override
    public String uploadFileStream(InputStream stream, String pathKey) {
        return uploadFileStream(stream, pathKey, config.getBucket());
    }

    @Override
    public String uploadFileStream(InputStream stream, String pathKey, String bucket) {
        checkArgument(stream != null, "stream can't be null");
        checkArgument(pathKey != null && pathKey.length() > 0, "pathKey can't be empty");
        checkArgument(bucket != null && bucket.length() > 0, "bucket can't be empty");

        OSS c = getOSSClient();
        ObjectMetadata metadata = new ObjectMetadata();
        c.putObject(bucket, pathKey, stream, metadata);
        return generateUrl(pathKey, bucket);
    }

    @Override
    public String generateUrl(String pathKey) {
        return generateUrl(pathKey, config.getBucket());
    }

    @Override
    public String generateUrl(String pathKey, String bucket) {
        try {
            String qs = URLEncoder.encode(pathKey, StandardCharsets.UTF_8.name());
            String host = config.getHostByBucket(bucket);
            return host + "/" + qs;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Never happen!", e);
        }
    }

    @Override
    public String generatePathKey(String url) {
        String urlPath = InnerUtils.getUrlPath(url);
        if (urlPath != null && urlPath.startsWith("/")) {
            String pathKey = urlPath.substring(1);
            if (pathKey.length() > 0) {
                return InnerUtils.decodeUrl(pathKey);
            }
        }
        return null;
    }

    @Override
    public String generateHost(String endpoint, String bucket) {
        return InnerUtils.generateHost(endpoint, bucket);
    }

}
