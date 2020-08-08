package com.github.lkqm.spring.aliyun.oss;

import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;

public class AliyunOSSConfigTest {

    @Test
    public void getRequestEndpoint() {
        AliyunOSSConfig config = new AliyunOSSConfig();
        config.setEndpoint("oss-cn-beijing.aliyuncs.com");
        Assert.assertEquals("无内网endpoint时，应该使用endpoint", config.getEndpoint(), config.getRequestEndpoint());

        config.setInternalEndpoint("oss-cn-beijing-internal.aliyuncs.com");
        Assert.assertEquals("配置了内网endpoint, 应该获取internalEndpoint", config.getInternalEndpoint(),
                config.getRequestEndpoint());
    }

    @Test
    public void getHostByBucket() {
        AliyunOSSConfig config = new AliyunOSSConfig();
        config.setEndpoint("oss-cn-beijing.aliyuncs.com");
        Assert.assertEquals("http://bucket1.oss-cn-beijing.aliyuncs.com", config.getHostByBucket("bucket1"));

        config.setBucketCustomDomain(new HashMap<String, String>() {{
            put("bucket1", "http://mario6.me");
        }});
        Assert.assertEquals("自定义域名优先", "http://mario6.me", config.getHostByBucket("bucket1"));
        Assert.assertEquals("http://bucket2.oss-cn-beijing.aliyuncs.com", config.getHostByBucket("bucket2"));
    }
}