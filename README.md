# aliyun-oss-spring-boot-starter ![Maven Central](https://img.shields.io/maven-central/v/com.github.lkqm/aliyun-oss-spring-boot-starter)

Aliyun oss spring boot starter.

Supports: JDK 1.7, spring-boot 1.5.x, spring-boot 2.x

## Features
- Spring Boot快速接入阿里云oss.
- 提供模版操作类`AliyunOSSTemplate`方便上传/下载等.
- 支持默认bucket、内网请求、自定义域名.


## Quick
1. 添加依赖
    ```
   <dependency>
       <groupId>com.github.lkqm</groupId>
       <artifactId>aliyun-oss-spring-boot-starter</artifactId>
       <version>${version}</version>
   </dependency>
    ```

2. 配置（application.properties)
    ```
   aliyun.oss.endpoint=@endpoint                    # 阿里云服务地址(必)
   aliyun.oss.internalEndpoint=@internalEndpoint    # 阿里云服务地址内网
   aliyun.oss.accessKeyId=@keyId                    # 访问key(必)
   aliyun.oss.accessKeySecret=@secret               # 访问密钥(必)
   aliyun.oss.regionId=@regionId                    # 基于STS授权的地区id
   aliyun.oss.roleArn=@roleArn                      # 基于STS授权的信息
   
   aliyun.oss.bucket=@bucket                        # 默认上传的空间(必)
   aliyun.oss.bucketCustomDomain.@bucket=@domain    # 配置自定义域名
   ```

3. 注入使用
    ```
    @Resource
    AliyunOSSTemplate aliyunOSSTemplate;

    @Test
    public void test() {
        String url = aliyunOSSTemplate.uploadFileText("test/hello.txt", "hello world!");
        System.out.println(url);
    }
    ```