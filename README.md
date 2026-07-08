# Java MCP Server

一个基于 [Anthropic Model Context Protocol](https://modelcontextprotocol.io/) 的 Java 服务端实现。

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+

### 构建与运行

```bash
# 1. 构建项目
mvn clean package -q

# 2. 启动 MCP Server（通过 stdio 通信）
java -jar target/mcp-server-1.0.0.jar
```

### 连接 AI 客户端

#### Claude Desktop 配置
在 `claude_desktop_config.json` 中添加：

```json
{
  "mcpServers": {
    "java-demo": {
      "command": "java",
      "args": ["-jar", "D:\\claude\\java-mcp-server\\target\\mcp-server-1.0.0.jar"]
    }
  }
}
```

#### VS Code / Cursor 配置
在 `.vscode/mcp.json` 或 Cursor 设置中添加相同配置。

## 已注册的工具

| 类别 | 名称 | 描述 |
|------|------|------|
| **Tools** | `get_current_time` | 获取当前日期时间 |
| | `days_between` | 计算两个日期的天数差 |
| | `calculate` | 执行数学计算 |
| | `convert_currency` | 货币换算（模拟汇率） |
| | `string_info` | 分析字符串信息 |
| | `transform_text` | 文本大小写/反转转换 |
| **Resources** | `system/info` | 读取服务器运行信息 |
| **Prompts** | `code_review` | 代码审查模板 |

## 项目结构

```
src/main/java/com/example/mcp/
├── McpServerApplication.java      # 主入口
├── tools/
│   ├── DateTool.java              # 日期工具
│   ├── CalculatorTool.java        # 计算器工具
│   └── TextTool.java             # 文本处理工具
├── resources/
│   └── SystemResource.java        # 系统信息资源
└── prompts/
    └── CodeReviewPrompt.java      # 代码审查提示词
```

## 扩展指南

添加新 Tool 只需三步：

```java
// 1. 实现 SyncToolProvider 接口
public class MyNewTool implements McpServerFeatures.SyncToolProvider {
    @Override
    public List<Tool> getTools() { ... }

    @Override
    public CallToolResult callTool(CallToolRequest req) { ... }
}

// 2. 在主类中注册
McpSyncServer server = McpServer.syncStdio(info)
    .tools(new MyNewTool())  // ← 注册
    .build();
```

## License
MIT
