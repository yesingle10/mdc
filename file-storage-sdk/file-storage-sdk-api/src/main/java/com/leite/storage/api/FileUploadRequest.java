package com.leite.storage.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件上传请求
 */
@Data
@Builder
public class FileUploadRequest {

    /** 文件原始名称 */
    private String fileName;

    /** 文件 MIME 类型 */
    private String contentType;

    /** 文件字节数组 */
    private byte[] content;

    /** 文件大小（字节） */
    private long fileSize;

    /** 租户 ID */
    private String tenantId;

    /** 文件分类：image / video / document */
    private Category category;

    /** 自定义文件路径（可选，为空则自动生成） */
    private String customKey;

    /** 自定义元数据 */
    private java.util.Map<String, String> metadata;

    public enum Category {
        IMAGE, VIDEO, DOCUMENT
    }
}
