package com.example.mcp.resources;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 系统信息资源
 * AI 可以通过 Resource 读取服务器运行的系统信息
 */
public class SystemResource implements McpServerFeatures.SyncResourceProvider {

    @Override
    public List<McpSchema.Resource> getResources() {
        return List.of(
                new McpSchema.Resource(
                        "system/info",
                        "系统运行信息",
                        "JSON",
                        "当前服务器的基本信息，包括 JVM 版本、操作系统、内存使用情况等"
                )
        );
    }

    @Override
    public McpSchema.ReadResourceResult readResource(McpSchema.ReadResourceRequest request) {
        String uri = request.uri();

        if ("system/info".equals(uri)) {
            Runtime rt = Runtime.getRuntime();
            long totalMemory = rt.totalMemory() / 1024 / 1024;
            long freeMemory = rt.freeMemory() / 1024 / 1024;
            long maxMemory = rt.maxMemory() / 1024 / 1024;

            String json = """
                    {
                      "jvm_version": "%s",
                      "os_name": "%s",
                      "os_arch": "%s",
                      "cpu_cores": %d,
                      "memory": {
                        "total_mb": %d,
                        "free_mb": %d,
                        "max_mb": %d,
                        "used_percent": %.1f%%
                      }
                    }
                    """.formatted(
                            System.getProperty("java.version"),
                            System.getProperty("os.name"),
                            System.getProperty("os.arch"),
                            rt.availableProcessors(),
                            totalMemory, freeMemory, maxMemory,
                            (double)(totalMemory - freeMemory) / totalMemory * 100
                    );

            return new McpSchema.ReadResourceResult(
                    List.of(new McpSchema.ResourceContents(json, "application/json", uri))
            );
        }

        throw new IllegalArgumentException("Unknown resource URI: " + uri);
    }
}
