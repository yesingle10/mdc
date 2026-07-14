package com.leite.storage.api.exception;

/**
 * 文件存储异常基类
 */
public abstract class FileStorageException extends RuntimeException {

    protected FileStorageException(String message) {
        super(message);
    }

    protected FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
