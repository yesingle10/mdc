package com.leite.storage.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件上传结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {

    /** 文件唯一标识（OSS 中的完整路径） */
    private String fileKey;

    /** 原始文件名 */
    private String fileName;

    /** MIME 类型 */
    private String contentType;

    /** 文件大小（字节） */
    private long fileSize;

    /** 访问 URL（CDN 域名 + fileKey） */
    private String url;

    /** 临时签名 URL */
    private String presignedUrl;

    /** 租户 ID */
    private String tenantId;

    /** 文件分类 */
    private String category;

    /** 上传时间 */
    private LocalDateTime uploadTime;

    /** 文件 MD5 */
    private String md5;
}
