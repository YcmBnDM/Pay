package com.example.components.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云对象存储（Aliyun OSS） 的yml配置参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "isys.oss.aliyun-oss")
public class AliyunOssYmlConfig {

    private String endpoint;

    // 公共读 桶名称
    private String publicBucketName;

    // 私有 桶名称
    private String privateBucketName;

    // AccessKeyId
    private String accessKeyId;
    // AccessKeySecret
    private String accessKeySecret;
}
