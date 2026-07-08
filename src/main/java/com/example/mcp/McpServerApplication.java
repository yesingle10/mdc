package com.example.mcp;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * MCP Server 主入口
 *
 * 启动方式：
 *   mvn clean package -q
 *   java -jar target/mcp-server-1.0.0.jar
 *
 * AI 客户端通过 stdio 与你通信，无需启动 HTTP 服务。
 */
public class McpServerApplication {
    private static final Logger log = LoggerFactory.getLogger(McpServerApplication.class);

    public static void main(String[] args) throws Exception {
        // 1. 创建 MCP 服务器配置
        McpSchema.ServerInfo serverInfo = new McpSchema.ServerInfo(
                "Java MCP Demo Server",
                "1.0.0"
        );

        // 2. 构建同步服务器（简单场景用 Sync 即可）
        McpSyncServer mcpServer = McpServer.syncStdio(serverInfo)
                // 注册工具（Tools）— AI 可以调用的方法
                .tools(List.of(
                        new DateTool(),
                        new CalculatorTool(),
                        new TextTool()
                ))
                // 注册资源（Resources）— AI 可以读取的数据源
                .resources(List.of(
                        new SystemResource()
                ))
                // 注册提示词模板（Prompts）— AI 可以使用的对话模板
                .prompts(List.of(
                        new CodeReviewPrompt()
                ))
                .build();

        log.info("✅ MCP Server 已启动，等待 AI 客户端连接...");

        // 3. 保持主线程运行
        Thread.currentThread().join();
    }
}
