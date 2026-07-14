package com.leite.storage.api;

import com.leite.storage.api.FileUploadRequest.Category;

/**
 * 文件存储服务核心接口
 * 其他系统通过引入此 SDK 即可使用文件上传、下载等功能
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param request 上传请求
     * @return 文件信息
     */
    FileInfo upload(FileUploadRequest request);

    /**
     * 上传文件（简化版，自动从 TenantContext 获取 tenantId）
     *
     * @param request 上传请求
     * @return 文件信息
     */
    FileInfo upload(FileUploadRequest.UploadRequestBuilder builder);

    /**
     * 下载文件（返回字节数组）
     *
     * @param fileKey 文件标识
     * @return 文件内容和元信息
     */
    byte[] download(String fileKey);

    /**
     * 下载文件（输出到流）
     *
     * @param fileKey 文件标识
     * @param outputStream 输出流
     */
    void download(String fileKey, java.io.OutputStream outputStream);

    /**
     * 删除文件
     *
     * @param fileKey 文件标识
     */
    void delete(String fileKey);

    /**
     * 批量删除
     *
     * @param fileKeys 文件标识列表
     * @return 批量操作结果
     */
    BatchResult batchDelete(List<String> fileKeys);

    /**
     * 获取临时签名 URL（默认 1 小时过期）
     *
     * @param fileKey 文件标识
     * @param expireMinutes 过期时间（分钟）
     * @return 临时访问 URL
     */
    String getPresignedUrl(String fileKey, int expireMinutes);

    /**
     * 获取文件元信息（不下载文件内容）
     *
     * @param fileKey 文件标识
     * @return 文件信息
     */
    FileInfo getFileInfo(String fileKey);

    /**
     * 检查文件是否存在
     *
     * @param fileKey 文件标识
     * @return true if exists
     */
    boolean exists(String fileKey);

    /**
     * 获取文件分类允许的最大大小（字节）
     *
     * @param category 文件分类
     * @return 最大文件大小
     */
    long getMaxFileSize(Category category);

    /**
     * 校验文件类型是否允许
     *
     * @param contentType MIME 类型
     * @param category 文件分类
     * @return true if allowed
     */
    boolean isAllowedType(String contentType, Category category);
}
