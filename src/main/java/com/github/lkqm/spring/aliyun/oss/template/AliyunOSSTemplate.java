package com.github.lkqm.spring.aliyun.oss.template;

import static com.github.lkqm.spring.aliyun.oss.template.InnerUtils.checkArgument;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.AppendObjectRequest;
import com.aliyun.oss.model.AppendObjectResult;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PolicyConditions;
import com.github.lkqm.spring.aliyun.oss.AliyunOSSConfig;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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
    public PostPolicyResult generateClientPolicy(String pathKey, int expireSeconds, long minSize, long maxSize) {
        return generateClientPolicy(config.getBucket(), pathKey, expireSeconds, minSize, maxSize);
    }

    @Override
    public PostPolicyResult generateClientPolicy(String bucket, String pathKey, int expireSeconds, long minSize,
            long maxSize) {
        OSS client = getOSSClient();

        long expireEndTime = System.currentTimeMillis() + expireSeconds * 1000;
        Date expiration = new Date(expireEndTime);
        PolicyConditions conditions = new PolicyConditions();
        conditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, minSize, maxSize);
        conditions.addConditionItem(MatchMode.Exact, PolicyConditions.COND_KEY, pathKey);

        String postPolicy = client.generatePostPolicy(expiration, conditions);
        byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = client.calculatePostSignature(postPolicy);

        PostPolicyResult data = new PostPolicyResult();
        data.setAccessKeyId(config.getAccessKeyId());
        data.setPolicy(encodedPolicy);
        data.setSignature(postSignature);
        data.setKey(pathKey);
        data.setExpireAt(expireEndTime);
        data.setHost(generateHost(config.getEndpoint(), bucket));
        data.setEndpoint(config.getEndpoint());
        data.setBucket(bucket);
        return data;
    }

    @Override
    public String uploadFileText(String pathKey, String content) {
        return uploadFileText(config.getBucket(), pathKey, content);
    }

    @Override
    public String uploadFileText(String bucket, String pathKey, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return uploadFileBytes(pathKey, bytes);
    }

    @Override
    public String uploadFileBase64Image(String pathKey, String content) {
        return uploadFileBase64Image(config.getBucket(), pathKey, content);
    }

    @Override
    public String uploadFileBase64Image(String bucket, String pathKey, String content) {
        byte[] bytes = Base64Utils.decodeFromString(content);
        return uploadFileBytes(bucket, pathKey, bytes);
    }

    @Override
    public String uploadFileBytes(String pathKey, byte[] bytes) {
        return uploadFileBytes(config.getBucket(), pathKey, bytes);
    }

    @Override
    public String uploadFileBytes(String bucket, String pathKey, byte[] bytes) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        return uploadFileStream(bucket, pathKey, stream);
    }

    @Override
    public String uploadFileStream(String pathKey, InputStream stream) {
        return uploadFileStream(config.getBucket(), pathKey, stream);
    }

    @Override
    public String uploadFileStream(String bucket, String pathKey, InputStream stream) {
        checkArgument(bucket != null && bucket.length() > 0, "bucket can't be empty");
        checkArgument(pathKey != null && pathKey.length() > 0, "pathKey can't be empty");
        checkArgument(stream != null, "stream can't be null");

        OSS c = getOSSClient();
        ObjectMetadata metadata = new ObjectMetadata();
        c.putObject(bucket, pathKey, stream, metadata);
        return generateUrl(pathKey, bucket);
    }

    @Override
    public AppendObjectResult appendObjectBytes(String pathKey, byte[] bytes, long position) {
        return appendObjectBytes(config.getBucket(), pathKey, bytes, position);
    }

    @Override
    public AppendObjectResult appendObjectBytes(String bucket, String pathKey, byte[] bytes, long position) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        return appendObjectStream(bucket, pathKey, stream, position);
    }

    @Override
    public AppendObjectResult appendObjectStream(String pathKey, InputStream stream, long position) {
        return appendObjectStream(config.getBucket(), pathKey, stream, position);
    }

    @Override
    public AppendObjectResult appendObjectStream(String bucket, String pathKey, InputStream stream, long position) {
        checkArgument(bucket != null && bucket.length() > 0, "bucket can't be empty");
        checkArgument(pathKey != null && pathKey.length() > 0, "pathKey can't be empty");
        checkArgument(stream != null, "stream can't be null");

        AppendObjectRequest appendObjectRequest = new AppendObjectRequest(bucket, pathKey, stream,
                new ObjectMetadata());
        appendObjectRequest.setPosition(position);
        return getOSSClient().appendObject(appendObjectRequest);
    }

    @Override
    public ObjectMetadata downloadFile(String pathKey, String file) {
        return downloadFile(config.getBucket(), pathKey, file);
    }

    @Override
    public ObjectMetadata downloadFile(String bucket, String pathKey, String file) {
        checkArgument(bucket != null && bucket.length() > 0, "bucket can't be empty");
        checkArgument(pathKey != null && pathKey.length() > 0, "pathKey can't be empty");
        checkArgument(file != null && file.length() > 0, "file can't be empty");
        GetObjectRequest request = new GetObjectRequest(bucket, pathKey);
        return getOSSClient().getObject(request, new File(file));
    }

    @Override
    public ObjectMetadata downloadFile(String pathKey, Consumer<InputStream> handler) {
        return downloadFile(config.getBucket(), pathKey, handler);
    }

    @Override
    public ObjectMetadata downloadFile(String bucket, String pathKey, Consumer<InputStream> handler) {
        checkArgument(bucket != null && bucket.length() > 0, "bucket can't be empty");
        checkArgument(pathKey != null && pathKey.length() > 0, "pathKey can't be empty");
        checkArgument(handler != null, "handler can't be null");

        OSSObject ossObject = getOSSClient().getObject(bucket, pathKey);
        InputStream content = ossObject.getObjectContent();
        try {
            handler.accept(content);
        } finally {
            if (content != null) {
                try {
                    content.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return ossObject.getObjectMetadata();
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
