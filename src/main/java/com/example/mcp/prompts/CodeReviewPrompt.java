package com.example.mcp.prompts;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;

/**
 * 代码审查提示词模板
 * AI 可以使用这个预定义模板来执行代码审查
 */
public class CodeReviewPrompt implements McpServerFeatures.SyncPromptProvider {

    @Override
    public List<McpSchema.Prompt> getPrompts() {
        return List.of(
                new McpSchema.Prompt(
                        "code_review",
                        "对代码进行审查，检查潜在问题",
                        List.of(
                                new McpSchema.PromptArgument(
                                        "language",
                                        "编程语言",
                                        true
                                ),
                                new McpSchema.PromptArgument(
                                        "code",
                                        "待审查的代码",
                                        true
                                )
                        )
                )
        );
    }

    @Override
    public McpSchema.GetPromptResult getPrompt(McpSchema.GetPromptRequest request) {
        if (!"code_review".equals(request.name())) {
            throw new IllegalArgumentException("Unknown prompt: " + request.name());
        }

        Map<String, Object> args = request.arguments();
        String language = (String) args.get("language");
        String code = (String) args.get("code");

        String description = """
                请用 %s 的风格审查以下代码，重点关注：
                1. 潜在的空指针异常
                2. 资源泄漏风险
                3. 性能瓶颈
                4. 安全性问题
                5. 代码规范

                代码内容：
                %s
                """.formatted(language, code);

        return new McpSchema.GetPromptResult(
                description,
                List.of(
                        new McpSchema.Message(
                                McpSchema.Role.USER,
                                new McpSchema.TextContent(description)
                        )
                )
        );
    }
}
