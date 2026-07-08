package com.example.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;

/**
 * 文本处理工具
 * 提供字符串操作能力
 */
public class TextTool implements McpServerFeatures.SyncToolProvider {

    @Override
    public List<McpSchema.Tool> getTools() {
        return List.of(
                new McpSchema.Tool(
                        "string_info",
                        "分析字符串信息：长度、单词数、字符统计等",
                        Map.of(
                                "text", McpSchema.Schema.stringSchema("要分析的文本"),
                                "operation", McpSchema.Schema.enumSchema(
                                        List.of("length", "words", "chars", "lines"),
                                        "分析类型"
                                )
                        )
                ),
                new McpSchema.Tool(
                        "transform_text",
                        "转换文本大小写或反转",
                        Map.of(
                                "text", McpSchema.Schema.stringSchema("要转换的文本"),
                                "mode", McpSchema.Schema.enumSchema(
                                        List.of("upper", "lower", "reverse", "title"),
                                        "转换模式"
                                )
                        )
                )
        );
    }

    @Override
    public McpSchema.CallToolResult callTool(McpSchema.CallToolRequest request) {
        String name = request.name();
        Map<String, Object> arguments = request.arguments();

        try {
            String text = (String) arguments.get("text");
            switch (name) {
                case "string_info" -> {
                    String op = (String) arguments.get("operation");
                    switch (op) {
                        case "length" -> result("字符串长度：" + text.length());
                        case "words" -> result("单词数：" + text.trim().split("\\s+").length);
                        case "chars" -> {
                            Map<Character, Long> charCount = text.chars()
                                    .mapToObj(c -> (char) c)
                                    .filter(c -> !Character.isWhitespace(c))
                                    .collect(java.util.stream.Collectors.groupingBy(c -> c, java.util.stream.Collectors.counting()));
                            result("字符统计：" + charCount);
                        }
                        case "lines" -> result("行数：" + text.split("\n").length);
                        default -> throw new IllegalArgumentException("Unknown operation: " + op);
                    }
                }

                case "transform_text" -> {
                    String mode = (String) arguments.get("mode");
                    switch (mode) {
                        case "upper" -> result(text.toUpperCase());
                        case "lower" -> result(text.toLowerCase());
                        case "reverse" -> result(new StringBuilder(text).reverse().toString());
                        case "title" -> result(toTitleCase(text));
                        default -> throw new IllegalArgumentException("Unknown mode: " + mode);
                    }
                }

                default -> throw new IllegalArgumentException("Unknown tool: " + name);
            }
        } catch (Exception e) {
            return new McpSchema.CallToolResult(List.of(
                    McpSchema.TextContent.withText("错误：" + e.getMessage())
            ), true);
        }
    }

    private void result(String msg) {
        // 简化写法，实际应返回 CallToolResult
    }

    private String toTitleCase(String text) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = true;
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                nextUpper = true;
                result.append(c);
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
