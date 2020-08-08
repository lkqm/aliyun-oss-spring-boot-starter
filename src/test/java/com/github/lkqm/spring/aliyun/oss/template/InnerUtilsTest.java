package com.github.lkqm.spring.aliyun.oss.template;

import java.io.UnsupportedEncodingException;
import org.junit.Assert;
import org.junit.Test;

public class InnerUtilsTest {

    @Test
    public void getUrlPath() throws UnsupportedEncodingException {
        Assert.assertEquals("/hello/world", InnerUtils.getUrlPath("http://mario6.me/hello/world"));
        Assert.assertEquals("/hello/world", InnerUtils.getUrlPath("http://mario6.me/hello/world?love=1"));
        Assert.assertEquals("/hello/谋", InnerUtils.getUrlPath("http://mario6.me/hello/谋"));
        Assert.assertEquals("/hello/谋", InnerUtils.getUrlPath("http://mario6.me/hello/%E8%B0%8B"));
    }

    @Test
    public void generateHost() {
        Assert.assertEquals("http://bucket2.mario6.me", InnerUtils.generateHost("http://mario6.me", "bucket2"));
        Assert.assertEquals("https://bucket2.mario6.me", InnerUtils.generateHost("https://mario6.me", "bucket2"));
        Assert.assertEquals("http://bucket1.mario6.me", InnerUtils.generateHost("mario6.me", "bucket1"));
    }

}