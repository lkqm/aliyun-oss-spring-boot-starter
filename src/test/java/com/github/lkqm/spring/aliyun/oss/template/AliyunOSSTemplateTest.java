package com.github.lkqm.spring.aliyun.oss.template;


import javax.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AliyunOSSTemplateTest {

    @Resource
    AliyunOSSTemplate aliyunOSSTemplate;

    @Test
    public void test() {
        String url = aliyunOSSTemplate.uploadFileText("test/hello.txt", "hello world!");
        System.out.println(url);
    }

    @Test
    public void generateSecurityToken() {
        SecurityTokenResult tokenResult = aliyunOSSTemplate
                .generateSecurityToken("test", "acs:ram::1905705565208367:role/osstestwrite", 900);
        System.out.println(tokenResult);
    }
}