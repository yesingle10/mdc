package com.leite.storage.api;

import java.util.List;

/**
 * 批量操作结果
 */
public record BatchResult(int successCount, int failCount, List<String> failedKeys) {
    public static BatchResult allSuccess(List<String> keys) {
        return new BatchResult(keys.size(), 0, List.of());
    }

    public static BatchResult allFail(List<String> keys) {
        return new BatchResult(0, keys.size(), keys);
    }
}
