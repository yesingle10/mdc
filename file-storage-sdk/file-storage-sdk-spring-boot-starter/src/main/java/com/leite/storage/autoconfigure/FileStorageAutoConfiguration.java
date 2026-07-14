package com.leite.storage.autoconfigure;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.leite.storage.api.FileStorageProperties;
import com.leite.storage.core.impl.AliyunOSSStorageServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储自动配置
 *
 * 使用方式：在 application.yml 中配置 file-storage 即可自动生效
 */
@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
@ConditionalOnProperty(prefix = "file-storage", name = "provider", havingValue = "aliyun-oss")
public class FileStorageAutoConfiguration {

    @Bean
    public OSS ossClient(FileStorageProperties properties) {
        return new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret()
        );
    }

    @Bean
    public AliyunOSSStorageServiceImpl fileStorageService(OSS ossClient, FileStorageProperties properties) {
        return new AliyunOSSStorageServiceImpl(ossClient, properties);
    }
}
