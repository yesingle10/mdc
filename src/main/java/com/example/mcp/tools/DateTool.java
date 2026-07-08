package com.example.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 日期时间工具
 * AI 可以通过这个 Tool 获取当前时间、格式化日期等
 */
public class DateTool implements McpServerFeatures.SyncToolProvider {

    @Override
    public List<McpSchema.Tool> getTools() {
        return List.of(
                // 获取当前时间
                new McpSchema.Tool(
                        "get_current_time",
                        "获取当前日期和时间，支持指定格式",
                        Map.of(
                                "format", McpSchema.Schema.stringSchema("日期格式，如 'yyyy-MM-dd HH:mm:ss'，默认为当前系统格式")
                        )
                ),
                // 计算两个日期之间的天数差
                new McpSchema.Tool(
                        "days_between",
                        "计算两个日期之间相隔的天数",
                        Map.of(
                                "start_date", McpSchema.Schema.stringSchema("开始日期，格式 yyyy-MM-dd"),
                                "end_date", McpSchema.Schema.stringSchema("结束日期，格式 yyyy-MM-dd")
                        )
                )
        );
    }

    @Override
    public McpSchema.CallToolResult callTool(McpSchema.CallToolRequest request) {
        String name = request.name();
        Map<String, Object> arguments = request.arguments();

        try {
            switch (name) {
                case "get_current_time" -> {
                    String format = (String) arguments.getOrDefault("format", "yyyy-MM-dd HH:mm:ss");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    String now = LocalDateTime.now().format(formatter);
                    return new McpSchema.CallToolResult(List.of(
                            McpSchema.TextContent.withText("当前时间：" + now)
                    ), false);
                }

                case "days_between" -> {
                    String startStr = (String) arguments.get("start_date");
                    String endStr = (String) arguments.get("end_date");
                    LocalDate start = LocalDate.parse(startStr);
                    LocalDate end = LocalDate.parse(endStr);
                    long days = ChronoUnit.DAYS.between(start, end);
                    return new McpSchema.CallToolResult(List.of(
                            McpSchema.TextContent.withText(startStr + " 到 " + endStr + " 相隔 " + Math.abs(days) + " 天")
                    ), false);
                }

                default -> throw new IllegalArgumentException("Unknown tool: " + name);
            }
        } catch (Exception e) {
            return new McpSchema.CallToolResult(List.of(
                    McpSchema.TextContent.withText("错误：" + e.getMessage())
            ), true);
        }
    }
}
