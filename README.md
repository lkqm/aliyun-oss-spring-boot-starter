# aliyun-oss-spring-boot-starter ![Maven Central](https://img.shields.io/maven-central/v/com.github.lkqm/aliyun-oss-spring-boot-starter)

Aliyun oss spring boot starter.

Supports: JDK 1.7, spring-boot 1.5.x, spring-boot 2.x

## Features
- Spring Boot快速接入阿里云oss.
- 支持默认bucket、内网请求、自定义域名、多种认证方式.
- 提供模版操作类`AliyunOssTemplate`方便上传/下载等.


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
   aliyun.oss.internal-endpoint=@internalEndpoint   # 阿里云服务地址内网
   aliyun.oss.region-id=@regionId                   # 地区标识(必)
   
   aliyun.oss.access-key-id=@keyId                  # 访问key(必)
   aliyun.oss.access-key-secret=@secret             # 访问密钥(必)
   aliyun.oss.security-token=@token                 # token
   aliyun.oss.role-arn=@roleArn                     # STS授权角色,如果不为空将使用STS构建OSS
   
   aliyun.oss.bucket=@bucket                        # 默认上传的空间(必)
   aliyun.oss.bucket-custom-domain.@bucket=@domain  # 配置自定义域名
   ```

3. 注入使用
    ```
    @Resource
    AliyunOssTemplate aliyunOssTemplate;
   
    @Resource
    OSS aliyunOssClient;

    @Test
    public void test() {
        String url = aliyunOssTemplate.uploadFileText("test/hello.txt", "hello world!");
        System.out.println(url);
    }
    ```