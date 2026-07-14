package com.leite.storage.api;

import lombok.Builder;
import lombok.Data;

/**
 * 文件存储服务配置
 */
@Data
@Builder
public class FileStorageProperties {

    /** 存储后端类型：aliyun-oss, qiniu, minio */
    private String provider;

    /** Bucket 名称 */
    private String bucket;

    /** Endpoint（如 oss-cn-hangzhou.aliyuncs.com） */
    private String endpoint;

    /** Access Key ID */
    private String accessKeyId;

    /** Access Key Secret */
    private String accessKeySecret;

    /** CDN 域名（可选，用于生成访问 URL） */
    private String cdnDomain;

    /** 是否开启 SSL */
    private boolean sslEnabled;

    /** 默认文件访问过期时间（分钟） */
    private int defaultExpireMinutes = 60;

    /** 各分类配置 */
    private CategoryConfig image = new CategoryConfig(CategoryType.IMAGE, 10, "jpg,jpeg,png,gif,webp");
    private CategoryConfig video = new CategoryConfig(CategoryType.VIDEO, 500, "mp4,avi,mov,wmv,mkv");
    private CategoryConfig document = new CategoryConfig(CategoryType.DOCUMENT, 50, "pdf,doc,docx,xls,xlsx,ppt,pptx,txt,csv");

    /** 分片上传阈值（字节），超过此大小自动分片上传 */
    private long multipartThreshold = 10 * 1024 * 1024;

    /** 分片大小（字节） */
    private long multipartPartSize = 5 * 1024 * 1024;

    /** 文件命名规则：按日期分目录 */
    private boolean enableDateDirectory = true;

    public enum CategoryType {
        IMAGE, VIDEO, DOCUMENT
    }

    @Data
    @Builder
    public static class CategoryConfig {
        private CategoryType type;
        /** 最大文件大小（MB） */
        private int maxFileSizeMB;
        /** 允许的文件扩展名，逗号分隔 */
        private String allowedExtensions;
    }
}
