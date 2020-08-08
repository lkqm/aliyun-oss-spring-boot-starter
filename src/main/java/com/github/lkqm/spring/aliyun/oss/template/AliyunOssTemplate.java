package com.github.lkqm.spring.aliyun.oss.template;

import static com.github.lkqm.spring.aliyun.oss.template.InnerUtils.checkArgument;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PolicyConditions;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.auth.sts.AssumeRoleResponse.Credentials;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.github.lkqm.spring.aliyun.oss.AliyunOssProperties;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.util.Base64Utils;

/**
 * 阿里云操作模版类, 简化常见操作
 */
@Getter
public class AliyunOssTemplate implements AliyunOssOptions {

    private final OSS ossClient;
    private final AliyunOssProperties ossProperties;

    public AliyunOssTemplate(OSS ossClient, AliyunOssProperties ossProperties) {
        this.ossClient = ossClient;
        this.ossProperties = ossProperties;
    }

    @Override
    public SecurityTokenResult generateSecurityToken(String sessionName, String roleArn, long durationSeconds) {
        return generateSecurityToken(ossProperties.getBucket(), sessionName, roleArn, durationSeconds);
    }

    @SneakyThrows
    @Override
    public SecurityTokenResult generateSecurityToken(String bucket, String sessionName, String roleArn,
            long durationSeconds) {
        AssumeRoleResponse roleResponse = assumeRoleResponse(sessionName, roleArn, durationSeconds);
        Credentials credentials = roleResponse.getCredentials();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        long expireAt = sdf.parse(credentials.getExpiration()).getTime();

        SecurityTokenResult token = new SecurityTokenResult();
        token.setAccessKeyId(credentials.getAccessKeyId());
        token.setAccessKeySecret(credentials.getAccessKeySecret());
        token.setSecurityToken(credentials.getSecurityToken());
        token.setExpireAt(expireAt);
        token.setEndpoint(ossProperties.getEndpoint());
        token.setBucket(bucket);
        token.setHost(ossProperties.getHostByBucket(bucket));
        return token;
    }

    /**
     * 获取授权信息
     */
    private AssumeRoleResponse assumeRoleResponse(String roleSessionName, String roleArn, long durationSeconds) {
        try {
            DefaultProfile.addEndpoint("", ossProperties.getRegionId(), "OSS", ossProperties.getEndpoint());
            IClientProfile profile = DefaultProfile
                    .getProfile(ossProperties.getRegionId(), ossProperties.getAccessKeyId(),
                            ossProperties.getAccessKeySecret());
            final DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setMethod(MethodType.POST);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setDurationSeconds(durationSeconds);
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public PostPolicyResult generateClientPolicy(String pathKey, int expireSeconds, long minSize, long maxSize) {
        return generateClientPolicy(ossProperties.getBucket(), pathKey, expireSeconds, minSize, maxSize);
    }

    @Override
    public PostPolicyResult generateClientPolicy(String bucket, String pathKey, int expireSeconds, long minSize,
            long maxSize) {
        long expireEndTime = System.currentTimeMillis() + expireSeconds * 1000;
        Date expiration = new Date(expireEndTime);
        PolicyConditions conditions = new PolicyConditions();
        conditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, minSize, maxSize);
        conditions.addConditionItem(MatchMode.Exact, PolicyConditions.COND_KEY, pathKey);

        String postPolicy = ossClient.generatePostPolicy(expiration, conditions);
        byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = ossClient.calculatePostSignature(postPolicy);

        PostPolicyResult data = new PostPolicyResult();
        data.setAccessKeyId(ossProperties.getAccessKeyId());
        data.setPolicy(encodedPolicy);
        data.setSignature(postSignature);
        data.setKey(pathKey);
        data.setExpireAt(expireEndTime);
        data.setHost(calculateHost(ossProperties.getEndpoint(), bucket));
        data.setEndpoint(ossProperties.getEndpoint());
        data.setBucket(bucket);
        return data;
    }

    @Override
    public String uploadFileText(String pathKey, String content) {
        return uploadFileText(ossProperties.getBucket(), pathKey, content);
    }

    @Override
    public String uploadFileText(String bucket, String pathKey, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return uploadFileBytes(pathKey, bytes);
    }

    @Override
    public String uploadFileBase64Image(String pathKey, String content) {
        return uploadFileBase64Image(ossProperties.getBucket(), pathKey, content);
    }

    @Override
    public String uploadFileBase64Image(String bucket, String pathKey, String content) {
        byte[] bytes = Base64Utils.decodeFromString(content);
        return uploadFileBytes(bucket, pathKey, bytes);
    }

    @Override
    public String uploadFileBytes(String pathKey, byte[] bytes) {
        return uploadFileBytes(ossProperties.getBucket(), pathKey, bytes);
    }

    @Override
    public String uploadFileBytes(String bucket, String pathKey, byte[] bytes) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        return uploadFileStream(bucket, pathKey, stream);
    }

    @Override
    public String uploadFileStream(String pathKey, InputStream stream) {
        return uploadFileStream(ossProperties.getBucket(), pathKey, stream);
    }

    @Override
    public String uploadFileStream(String bucket, String pathKey, InputStream stream) {
        checkArgument(bucket != null && bucket.length() > 0, "bucket can't be empty");
        checkArgument(pathKey != null && pathKey.length() > 0, "pathKey can't be empty");
        checkArgument(stream != null, "stream can't be null");

        ObjectMetadata metadata = new ObjectMetadata();
        ossClient.putObject(bucket, pathKey, stream, metadata);
        return calculateUrl(pathKey, bucket);
    }

    @Override
    public File downloadFileTmp(String pathKey) {
        return downloadFileTmp(ossProperties.getBucket(), pathKey);
    }

    @Override
    public File downloadFileTmp(String bucket, String pathKey) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String file = tmpDir + UUID.randomUUID();
        downloadFile(bucket, pathKey, file);
        return new File(file);
    }

    @Override
    public ObjectMetadata downloadFile(String pathKey, String file) {
        return downloadFile(ossProperties.getBucket(), pathKey, file);
    }

    @Override
    public ObjectMetadata downloadFile(String bucket, String pathKey, String file) {
        checkArgument(bucket != null && bucket.length() > 0, "bucket can't be empty");
        checkArgument(pathKey != null && pathKey.length() > 0, "pathKey can't be empty");
        checkArgument(file != null && file.length() > 0, "file can't be empty");
        GetObjectRequest request = new GetObjectRequest(bucket, pathKey);
        return ossClient.getObject(request, new File(file));
    }

    @Override
    public ObjectMetadata downloadFile(String pathKey, Consumer<InputStream> handler) {
        return downloadFile(ossProperties.getBucket(), pathKey, handler);
    }

    @Override
    public ObjectMetadata downloadFile(String bucket, String pathKey, Consumer<InputStream> handler) {
        checkArgument(bucket != null && bucket.length() > 0, "bucket can't be empty");
        checkArgument(pathKey != null && pathKey.length() > 0, "pathKey can't be empty");
        checkArgument(handler != null, "handler can't be null");

        OSSObject ossObject = ossClient.getObject(bucket, pathKey);
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
    public String calculateUrl(String pathKey) {
        return calculateUrl(ossProperties.getBucket(), pathKey);
    }

    @Override
    public String calculateUrl(String bucket, String pathKey) {
        try {
            String qs = URLEncoder.encode(pathKey, StandardCharsets.UTF_8.name());
            String host = ossProperties.getHostByBucket(bucket);
            return host + "/" + qs;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Never happen!", e);
        }
    }

    @Override
    public String calculatePathKey(String url) {
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
    public String calculateHost(String endpoint, String bucket) {
        return InnerUtils.generateHost(endpoint, bucket);
    }

}
