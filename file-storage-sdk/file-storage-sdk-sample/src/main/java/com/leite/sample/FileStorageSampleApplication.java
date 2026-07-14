package com.leite.sample;

import com.leite.storage.api.FileInfo;
import com.leite.storage.api.FileStorageProperties;
import com.leite.storage.api.FileUploadRequest;
import com.leite.storage.api.FileStorageService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * 文件存储集成示例
 *
 * 其他系统集成方式：
 * 1. 引入 file-storage-sdk-spring-boot-starter 依赖
 * 2. 在 application.yml 中配置 file-storage
 * 3. 注入 FileStorageService 直接使用
 */
@SpringBootApplication
@RestController
@RequestMapping("/api/files")
public class FileStorageSampleApplication {

    private final FileStorageService fileStorageService;

    public FileStorageSampleApplication(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public static void main(String[] args) {
        SpringApplication.run(FileStorageSampleApplication.class, args);
    }

    /**
     * 上传文件
     *
     * POST /api/files/upload
     * 参数：
     *   file: 文件
     *   tenantId: 租户 ID
     *   category: image / video / document
     */
    @PostMapping("/upload")
    public FileInfo upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tenantId") String tenantId,
            @RequestParam(value = "category", defaultValue = "DOCUMENT") String category
    ) {
        FileUploadRequest.Category cat = switch (category.toUpperCase()) {
            case "IMAGE" -> FileUploadRequest.Category.IMAGE;
            case "VIDEO" -> FileUploadRequest.Category.VIDEO;
            default -> FileUploadRequest.Category.DOCUMENT;
        };

        return fileStorageService.upload(FileUploadRequest.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .content(file.getBytes())
                .fileSize(file.getSize())
                .tenantId(tenantId)
                .category(cat)
                .build());
    }

    /**
     * 下载文件（通过签名 URL 跳转）
     *
     * GET /api/files/{fileKey}/presign
     */
    @GetMapping("/{fileKey}/presign")
    public String presign(@PathVariable String fileKey,
                          @RequestParam(value = "expire", defaultValue = "60") int expireMinutes) {
        return fileStorageService.getPresignedUrl(fileKey, expireMinutes);
    }

    /**
     * 删除文件
     *
     * DELETE /api/files/{fileKey}
     */
    @DeleteMapping("/{fileKey}")
    public String delete(@PathVariable String fileKey) {
        fileStorageService.delete(fileKey);
        return "deleted: " + fileKey;
    }

    /**
     * 批量删除
     *
     * POST /api/files/batch-delete
     */
    @PostMapping("/batch-delete")
    public com.leite.storage.api.BatchResult batchDelete(
            @RequestBody List<String> fileKeys) {
        return fileStorageService.batchDelete(fileKeys);
    }

    /**
     * 获取文件信息
     *
     * GET /api/files/{fileKey}/info
     */
    @GetMapping("/{fileKey}/info")
    public FileInfo fileInfo(@PathVariable String fileKey) {
        return fileStorageService.getFileInfo(fileKey);
    }

    /**
     * 检查文件是否存在
     *
     * GET /api/files/{fileKey}/exists
     */
    @GetMapping("/{fileKey}/exists")
    public boolean exists(@PathVariable String fileKey) {
        return fileStorageService.exists(fileKey);
    }
}
