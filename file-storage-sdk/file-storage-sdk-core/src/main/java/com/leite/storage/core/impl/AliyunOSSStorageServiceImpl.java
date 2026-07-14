package com.leite.storage.core.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.leite.storage.api.*;
import com.leite.storage.api.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 阿里云 OSS 文件存储服务实现
 *
 * 文件路径规则：
 *   {category}/{tenantId}/{yyyyMMdd}/{randomUUID}.{ext}
 *
 * 示例：
 *   image/tenant001/20260715/a1b2c3d4-e5f6.jpg
 *   video/tenant001/20260715/f7g8h9i0.mp4
 *   document/tenant002/20260715/j1k2l3m4.pdf
 */
@Slf4j
public class AliyunOSSStorageServiceImpl implements FileStorageService {

    private final OSS ossClient;
    private final String bucketName;
    private final String cdnDomain;
    private final FileStorageProperties properties;

    public AliyunOSSStorageServiceImpl(OSS ossClient, FileStorageProperties properties) {
        this.ossClient = ossClient;
        this.bucketName = properties.getBucket();
        this.cdnDomain = properties.getCdnDomain();
        this.properties = properties;
    }

    // ==================== 上传 ====================

    @Override
    public FileInfo upload(FileUploadRequest request) {
        // 1. 校验文件
        validateUpload(request);

        // 2. 生成文件 Key
        String fileKey = request.getCustomKey() != null
                ? request.getCustomKey()
                : generateFileKey(request);

        // 3. 计算 MD5
        String md5 = DigestUtils.md5Hex(request.getContent());

        // 4. 上传（判断是否需要分片上传）
        if (request.getFileSize() >= properties.getMultipartThreshold()) {
            uploadMultipart(fileKey, request);
        } else {
            uploadSimple(fileKey, request);
        }

        // 5. 构建返回结果
        String url = buildUrl(fileKey);
        String presignedUrl = getPresignedUrl(fileKey, properties.getDefaultExpireMinutes());

        return FileInfo.builder()
                .fileKey(fileKey)
                .fileName(request.getFileName())
                .contentType(request.getContentType())
                .fileSize(request.getFileSize())
                .url(url)
                .presignedUrl(presignedUrl)
                .tenantId(request.getTenantId())
                .category(request.getCategory() != null ? request.getCategory().name().toLowerCase() : null)
                .uploadTime(LocalDateTime.now())
                .md5(md5)
                .build();
    }

    private void uploadSimple(String fileKey, FileUploadRequest request) {
        try {
            PutObjectResult result = ossClient.putObject(
                    bucketName, fileKey,
                    new ByteArrayInputStream(request.getContent())
            );
            log.debug("Simple upload success: key={}, eTag={}", fileKey, result.getETag());
        } catch (Exception e) {
            throw new FileUploadException("文件上传失败: " + e.getMessage(), e);
        }
    }

    private void uploadMultipart(String fileKey, FileUploadRequest request) {
        long partSize = properties.getMultipartPartSize();
        byte[] content = request.getContent();
        long totalParts = (long) Math.ceil((double) content.length / partSize);

        log.info("Starting multipart upload: key={}, totalSize={} bytes, parts={}",
                fileKey, content.length, totalParts);

        try {
            // 1. 初始化
            InitiateMultipartUploadResult initResult = ossClient.initiateMultipartUpload(
                    new InitiateMultipartUploadRequest(bucketName, fileKey));
            String uploadId = initResult.getUploadId();

            // 2. 分片上传
            List<PartETag> partETags = new ArrayList<>();
            for (int i = 0; i < totalParts; i++) {
                long start = i * partSize;
                long end = Math.min(start + partSize, content.length);
                int currentPartSize = (int) (end - start);

                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(fileKey);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setPartNumber(i + 1);
                uploadPartRequest.setPartSize(currentPartSize);
                uploadPartRequest.setInputStream(
                        new ByteArrayInputStream(content, (int) start, currentPartSize));

                PartETag partETag = ossClient.uploadPart(uploadPartRequest).getPartETag();
                partETags.add(partETag);

                log.debug("Uploaded part {}/{}", i + 1, totalParts);
            }

            // 3. 完成
            ossClient.completeMultipartUpload(
                    new CompleteMultipartUploadRequest(bucketName, fileKey, uploadId, partETags));

            log.info("Multipart upload completed: key={}", fileKey);

        } catch (Exception e) {
            // 失败则中止
            try {
                ossClient.abortMultipartUpload(
                        new AbortMultipartUploadRequest(bucketName, fileKey, uploadId));
            } catch (Exception abortEx) {
                log.warn("Failed to abort multipart upload: {}", abortEx.getMessage());
            }
            throw new FileUploadException("大文件上传失败: " + e.getMessage(), e);
        }
    }

    // ==================== 下载 ====================

    @Override
    public byte[] download(String fileKey) {
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, fileKey);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = ossObject.getObjectContent().read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new FileUploadException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void download(String fileKey, java.io.OutputStream outputStream) {
        OSSObject ossObject = ossClient.getObject(bucketName, fileKey);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(
                download(fileKey))) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            throw new FileUploadException("文件输出失败: " + e.getMessage(), e);
        }
    }

    // ==================== 删除 ====================

    @Override
    public void delete(String fileKey) {
        ossClient.deleteObject(bucketName, fileKey);
        log.info("Deleted file: {}", fileKey);
    }

    @Override
    public BatchResult batchDelete(List<String> fileKeys) {
        int successCount = 0;
        List<String> failedKeys = new ArrayList<>();
        for (String key : fileKeys) {
            try {
                delete(key);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to delete file: {}", key, e);
                failedKeys.add(key);
            }
        }
        return new BatchResult(successCount, failedKeys.size(), failedKeys);
    }

    // ==================== 签名 URL ====================

    @Override
    public String getPresignedUrl(String fileKey, int expireMinutes) {
        java.util.Date expiration = new java.util.Date(
                System.currentTimeMillis() + expireMinutes * 60L * 1000L);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                bucketName, fileKey, HttpMethod.GET);
        request.setExpiration(expiration);

        URL url = ossClient.generatePresignedUrl(request);
        return url.toString();
    }

    // ==================== 元信息 ====================

    @Override
    public FileInfo getFileInfo(String fileKey) {
        try {
            ObjectMetadata metadata = ossClient.getObjectMetadata(bucketName, fileKey);
            if (metadata == null) {
                return null;
            }

            String url = buildUrl(fileKey);
            return FileInfo.builder()
                    .fileKey(fileKey)
                    .fileName(metadata.getContentDisposition())
                    .contentType(metadata.getContentType())
                    .fileSize(metadata.getContentLength())
                    .url(url)
                    .uploadTime(LocalDateTime.now())
                    .build();
        } catch (OSSException e) {
            if (e.getHttpStatusCode() == 404) {
                return null;
            }
            throw new FileUploadException("获取文件信息失败: " + e.getErrorMessage(), e);
        }
    }

    @Override
    public boolean exists(String fileKey) {
        try {
            return ossClient.doesObjectExist(bucketName, fileKey);
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 校验 ====================

    @Override
    public long getMaxFileSize(FileUploadRequest.Category category) {
        switch (category) {
            case IMAGE: return properties.getImage().getMaxFileSizeMB() * 1024L * 1024L;
            case VIDEO: return properties.getVideo().getMaxFileSizeMB() * 1024L * 1024L;
            case DOCUMENT: return properties.getDocument().getMaxFileSizeMB() * 1024L * 1024L;
            default: return 10L * 1024 * 1024;
        }
    }

    @Override
    public boolean isAllowedType(String contentType, FileUploadRequest.Category category) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        String ext = getExtensionFromMimeType(contentType);
        String allowed = switch (category) {
            case IMAGE -> properties.getImage().getAllowedExtensions();
            case VIDEO -> properties.getVideo().getAllowedExtensions();
            case DOCUMENT -> properties.getDocument().getAllowedExtensions();
        };
        return allowed != null && allowed.split(",").length > 0
                && java.util.Arrays.asList(allowed.split(",")).contains(ext);
    }

    // ==================== 私有方法 ====================

    private void validateUpload(FileUploadRequest request) {
        if (request == null || request.getContent() == null || request.getContent().length == 0) {
            throw new FileUploadException("文件内容不能为空");
        }

        FileUploadRequest.Category category = request.getCategory();
        if (category == null) {
            throw new FileUploadException("文件分类不能为空");
        }

        // 大小校验
        long maxBytes = getMaxFileSize(category);
        if (request.getFileSize() > maxBytes) {
            throw new FileUploadException(
                    String.format("%s 类型文件最大允许 %d MB", category, maxBytes / 1024 / 1024));
        }

        // 类型校验
        if (!isAllowedType(request.getContentType(), category)) {
            throw new FileUploadException("不支持的文件类型: " + request.getContentType());
        }

        // 租户 ID 校验
        if (request.getTenantId() == null || request.getTenantId().isEmpty()) {
            throw new FileUploadException("租户 ID 不能为空");
        }
    }

    private String generateFileKey(FileUploadRequest request) {
        String category = request.getCategory().name().toLowerCase();
        String tenantId = request.getTenantId();
        String dateDir = properties.isEnableDateDirectory()
                ? LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                : "";
        String ext = getExtension(request.getFileName());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        StringBuilder key = new StringBuilder();
        key.append(category).append("/").append(tenantId).append("/");
        if (!dateDir.isEmpty()) {
            key.append(dateDir).append("/");
        }
        key.append(uuid).append(".").append(ext);

        return key.toString();
    }

    private String getExtension(String fileName) {
        if (fileName == null) return "bin";
        String ext = FilenameUtils.getExtension(fileName);
        return ext.isEmpty() ? "bin" : ext;
    }

    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) return "";
        return switch (mimeType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "video/mp4" -> "mp4";
            case "video/avi" -> "avi";
            case "video/quicktime" -> "mov";
            case "video/x-msvideo" -> "avi";
            case "application/pdf" -> "pdf";
            case "application/msword" -> "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx";
            case "application/vnd.ms-excel" -> "xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx";
            case "text/plain" -> "txt";
            case "application/csv" -> "csv";
            default -> mimeType.split("/")[1].split(";")[0];
        };
    }

    private String buildUrl(String fileKey) {
        if (cdnDomain != null && !cdnDomain.isEmpty()) {
            return cdnDomain + "/" + fileKey;
        }
        return properties.getEndpoint() + "/" + bucketName + "/" + fileKey;
    }
}
