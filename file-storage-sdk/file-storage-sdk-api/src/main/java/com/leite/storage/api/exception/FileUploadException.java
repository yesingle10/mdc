package com.leite.storage.api.exception;

/**
 * 文件上传异常
 */
public class FileUploadException extends FileStorageException {
    public FileUploadException(String message) {
        super(message);
    }
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
