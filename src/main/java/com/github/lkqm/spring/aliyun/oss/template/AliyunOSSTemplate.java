package com.github.lkqm.spring.aliyun.oss.template;

import static com.github.lkqm.spring.aliyun.oss.template.InnerUtils.checkArgument;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
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
import com.github.lkqm.spring.aliyun.oss.AliyunOSSConfig;
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
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.util.Base64Utils;

/**
 * 阿里云操作模版类, 简化常见操作
 */
public class AliyunOSSTemplate implements AliyunOSSOptions {

    @Getter
    private final AliyunOSSConfig ossConfig;
    private volatile OSS ossClient;

    /**
     * 是否基于STS的授权
     */
    private final boolean roleArn;
    /**
     * 控制STS授权过期时间
     */
    private volatile long clientExpireTime;
    private static final long clientDurationSeconds = 3600L;
    private static final long expireDurationSeconds = clientDurationSeconds - 60;

    public AliyunOSSTemplate(AliyunOSSConfig ossConfig) {
        this.ossConfig = ossConfig;
        if (this.ossConfig.getRoleArn() != null && this.ossConfig.getRoleArn().length() > 0) {
            this.roleArn = true;
        } else {
            this.roleArn = false;
        }
    }

    @Override
    public OSS createOSSClient() {
        if (roleArn) {
            String requestEndpoint = ossConfig.getRequestEndpoint();
            AssumeRoleResponse roleResponse = this
                    .assumeRoleResponse(ossConfig.getAccessKeyId(), ossConfig.getRoleArn(), clientDurationSeconds);
            AssumeRoleResponse.Credentials credentials = roleResponse.getCredentials();
            return new OSSClientBuilder().build(
                    requestEndpoint,
                    credentials.getAccessKeyId(),
                    credentials.getAccessKeySecret(),
                    credentials.getSecurityToken()
            );
        } else {
            return ossConfig.createOSSClient();
        }
    }

    /**
     * 不要关闭返回的OSS客户端
     */
    @Override
    public OSS getOSSClient() {
        if (ossClient == null) {
            synchronized (this) {
                if (ossClient == null) {
                    ossClient = createOSSClient();
                    clientExpireTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireDurationSeconds);
                }
            }
        } else if (roleArn && clientExpireTime < System.currentTimeMillis()) {
            synchronized (this) {
                if (clientExpireTime < System.currentTimeMillis()) {
                    ossClient = createOSSClient();
                    clientExpireTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireDurationSeconds);
                }
            }
        }
        return ossClient;
    }

    @Override
    public SecurityTokenResult generateSecurityToken(String sessionName, String roleArn, long durationSeconds) {
        return generateSecurityToken(ossConfig.getBucket(), sessionName, roleArn, durationSeconds);
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
        token.setEndpoint(ossConfig.getEndpoint());
        token.setBucket(bucket);
        token.setHost(ossConfig.getHostByBucket(bucket));
        return token;
    }

    /**
     * 获取授权信息
     */
    private AssumeRoleResponse assumeRoleResponse(String roleSessionName, String roleArn, long durationSeconds) {
        try {
            DefaultProfile.addEndpoint("", ossConfig.getRegionId(), "OSS", ossConfig.getEndpoint());
            IClientProfile profile = DefaultProfile
                    .getProfile(ossConfig.getRegionId(), ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());
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
        return generateClientPolicy(ossConfig.getBucket(), pathKey, expireSeconds, minSize, maxSize);
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
        data.setAccessKeyId(ossConfig.getAccessKeyId());
        data.setPolicy(encodedPolicy);
        data.setSignature(postSignature);
        data.setKey(pathKey);
        data.setExpireAt(expireEndTime);
        data.setHost(generateHost(ossConfig.getEndpoint(), bucket));
        data.setEndpoint(ossConfig.getEndpoint());
        data.setBucket(bucket);
        return data;
    }

    @Override
    public String uploadFileText(String pathKey, String content) {
        return uploadFileText(ossConfig.getBucket(), pathKey, content);
    }

    @Override
    public String uploadFileText(String bucket, String pathKey, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return uploadFileBytes(pathKey, bytes);
    }

    @Override
    public String uploadFileBase64Image(String pathKey, String content) {
        return uploadFileBase64Image(ossConfig.getBucket(), pathKey, content);
    }

    @Override
    public String uploadFileBase64Image(String bucket, String pathKey, String content) {
        byte[] bytes = Base64Utils.decodeFromString(content);
        return uploadFileBytes(bucket, pathKey, bytes);
    }

    @Override
    public String uploadFileBytes(String pathKey, byte[] bytes) {
        return uploadFileBytes(ossConfig.getBucket(), pathKey, bytes);
    }

    @Override
    public String uploadFileBytes(String bucket, String pathKey, byte[] bytes) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        return uploadFileStream(bucket, pathKey, stream);
    }

    @Override
    public String uploadFileStream(String pathKey, InputStream stream) {
        return uploadFileStream(ossConfig.getBucket(), pathKey, stream);
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
    public ObjectMetadata downloadFile(String pathKey, String file) {
        return downloadFile(ossConfig.getBucket(), pathKey, file);
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
        return downloadFile(ossConfig.getBucket(), pathKey, handler);
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
        return generateUrl(pathKey, ossConfig.getBucket());
    }

    @Override
    public String generateUrl(String pathKey, String bucket) {
        try {
            String qs = URLEncoder.encode(pathKey, StandardCharsets.UTF_8.name());
            String host = ossConfig.getHostByBucket(bucket);
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
