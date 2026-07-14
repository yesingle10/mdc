# Leite File Storage SDK

公共文件存储服务 SDK，支持图片、视频、文档上传下载，内置多租户隔离、分片上传、签名 URL 等能力。

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.leite</groupId>
    <artifactId>file-storage-sdk-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置 application.yml

```yaml
file-storage:
  provider: aliyun-oss
  bucket: leite
  endpoint: oss-cn-hangzhou.aliyuncs.com
  access-key-id: YOUR_ACCESS_KEY_ID
  access-key-secret: YOUR_ACCESS_KEY_SECRET
  cdn-domain: https://cdn.leite.com
```

### 3. 注入使用

```java
@Service
public class DocumentService {
    @Autowired
    private FileStorageService fileStorageService;

    public FileInfo uploadDocument(byte[] content, String fileName, String tenantId) {
        return fileStorageService.upload(FileUploadRequest.builder()
                .fileName(fileName)
                .contentType("application/pdf")
                .content(content)
                .fileSize(content.length)
                .tenantId(tenantId)
                .category(FileUploadRequest.Category.DOCUMENT)
                .build());
    }
}
```

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/files/upload | 上传文件 |
| GET | /api/files/{key}/presign | 获取临时访问链接 |
| DELETE | /api/files/{key} | 删除文件 |
| POST | /api/files/batch-delete | 批量删除 |
| GET | /api/files/{key}/info | 获取文件信息 |
| GET | /api/files/{key}/exists | 检查文件是否存在 |

## 文件路径规则

```
{category}/{tenantId}/{yyyyMMdd}/{uuid}.{ext}

示例：
  image/tenant001/20260715/a1b2c3d4e5f67890.jpg
  video/tenant001/20260715/b2c3d4e5f6789012.mp4
  document/tenant002/20260715/c3d4e5f678901234.pdf
```

## 支持的文件类型

| 分类 | 最大大小 | 允许格式 |
|------|---------|---------|
| 图片 | 10 MB | jpg, jpeg, png, gif, webp |
| 视频 | 500 MB | mp4, avi, mov, wmv, mkv |
| 文档 | 50 MB | pdf, doc, docx, xls, xlsx, ppt, pptx, txt, csv |

## 项目结构

```
file-storage-sdk/
├── file-storage-sdk-api/              # 接口定义（其他系统只需引入这个 + starter）
├── file-storage-sdk-core/             # 核心实现（阿里云 OSS）
├── file-storage-sdk-spring-boot-starter/ # Spring Boot 自动配置
└── file-storage-sdk-sample/           # 集成示例
```

## 多租户隔离

每个文件上传必须携带 `tenantId`，系统自动按租户隔离存储路径，确保数据安全。

## 大文件支持

超过 10 MB 的文件自动启用分片上传，支持断点续传（阿里云 SDK 内置）。
