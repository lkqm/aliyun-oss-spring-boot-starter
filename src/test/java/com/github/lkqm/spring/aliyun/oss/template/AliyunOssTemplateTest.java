package com.github.lkqm.spring.aliyun.oss.template;


import com.github.lkqm.spring.aliyun.oss.AliyunOssProperties;
import java.io.File;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AliyunOssTemplateTest {

    @Resource
    AliyunOssTemplate aliyunOssTemplate;

    @Test
    public void test() {
        String pathKey = "test/hello.txt";
        String content = "hello world";
        String url = aliyunOssTemplate.uploadFileText(pathKey, content);
        Assert.assertNotNull(url);

        File file = aliyunOssTemplate.downloadFileTmp(pathKey);
        Assert.assertTrue(file != null && file.exists());
    }

    @Test
    public void generateSecurityToken() {
        SecurityTokenResult tokenResult = aliyunOssTemplate
                .generateSecurityToken("test", "acs:ram::1905705565208367:role/osstestwrite", 900);
        Assert.assertNotNull(tokenResult);
        // 认证相关数据
        Assert.assertNotNull(tokenResult.getAccessKeyId());
        Assert.assertNotNull(tokenResult.getAccessKeySecret());
        Assert.assertNotNull(tokenResult.getSecurityToken());
        Assert.assertNotNull(tokenResult.getExpireAt());

        // 附加数据
        AliyunOssProperties ossConfig = aliyunOssTemplate.getOssProperties();
        Assert.assertEquals(ossConfig.getEndpoint(), tokenResult.getEndpoint());
        Assert.assertEquals(ossConfig.getBucket(), tokenResult.getBucket());
        Assert.assertEquals(ossConfig.getHostByBucket(ossConfig.getBucket()), tokenResult.getHost());
    }

    @Test
    public void generateClientPolicy() {
        PostPolicyResult policy = aliyunOssTemplate.generateClientPolicy("hello/world.txt", 60, 0, 10 * 1024 * 1034);
        // 业务结果
        Assert.assertNotNull(policy);
        Assert.assertNotNull(policy.getPolicy());
        Assert.assertNotNull(policy.getSignature());

        // 附加数据
        AliyunOssProperties ossConfig = aliyunOssTemplate.getOssProperties();
        Assert.assertEquals(ossConfig.getEndpoint(), policy.getEndpoint());
        Assert.assertEquals(ossConfig.getBucket(), policy.getBucket());
        Assert.assertEquals(ossConfig.getHostByBucket(ossConfig.getBucket()), policy.getHost());
    }

    @Test
    public void calculateUrl() {
        AliyunOssProperties ossConfig = aliyunOssTemplate.getOssProperties();
        ossConfig.setEndpoint("oss-cn-beijing.aliyuncs.com");
        String url = aliyunOssTemplate.calculateUrl("bucket1", "hello.txt");
        Assert.assertEquals("http://bucket1.oss-cn-beijing.aliyuncs.com/hello.txt", url);
    }

    @Test
    public void calculatePathKey() {
        String pathKey = aliyunOssTemplate.calculatePathKey("http://bucket1.oss-cn-beijing.aliyuncs.com/hello.txt");
        Assert.assertEquals("hello.txt", pathKey);
    }

    @Test
    public void calculateHost() {
        String host = aliyunOssTemplate.calculateHost("oss-cn-beijing.aliyuncs.com", "bucket1");
        Assert.assertEquals("http://bucket1.oss-cn-beijing.aliyuncs.com", host);
    }
}