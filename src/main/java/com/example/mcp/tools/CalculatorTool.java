package com.example.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * 计算器工具
 * 支持基础数学运算和复杂公式
 */
public class CalculatorTool implements McpServerFeatures.SyncToolProvider {

    @Override
    public List<McpSchema.Tool> getTools() {
        return List.of(
                new McpSchema.Tool(
                        "calculate",
                        "执行数学计算，支持 +, -, *, /, ^, sqrt, abs 等",
                        Map.of(
                                "expression", McpSchema.Schema.stringSchema("数学表达式，例如 '2 + 3 * 4' 或 'sqrt(144)'"),
                                "precision", McpSchema.Schema.integerSchema("小数位数，默认 2")
                        )
                ),
                new McpSchema.Tool(
                        "convert_currency",
                        "货币汇率换算（模拟汇率）",
                        Map.of(
                                "amount", McpSchema.Schema.numberSchema("金额"),
                                "from_currency", McpSchema.Schema.enumSchema(List.of("CNY", "USD", "EUR", "JPY"), "源币种"),
                                "to_currency", McpSchema.Schema.enumSchema(List.of("CNY", "USD", "EUR", "JPY"), "目标币种")
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
                case "calculate" -> {
                    String expression = (String) arguments.get("expression");
                    int precision = ((Number) arguments.getOrDefault("precision", 2)).intValue();
                    BigDecimal result = evaluateExpression(expression, precision);
                    return new McpSchema.CallToolResult(List.of(
                            McpSchema.TextContent.withText(expression + " = " + result.toPlainString())
                    ), false);
                }

                case "convert_currency" -> {
                    double amount = ((Number) arguments.get("amount")).doubleValue();
                    String from = (String) arguments.get("from_currency");
                    String to = (String) arguments.get("to_currency");
                    double rate = getExchangeRate(from, to);
                    double converted = amount * rate;
                    return new McpSchema.CallToolResult(List.of(
                            McpSchema.TextContent.withText(
                                    String.format("%.2f %s = %.2f %s (汇率: 1 %s = %.4f %s)",
                                            amount, from, converted, to, from, rate, to)
                            )
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

    private BigDecimal evaluateExpression(String expr, int precision) {
        // 简单实现：支持基本运算符
        expr = expr.trim();

        // 处理 sqrt
        if (expr.startsWith("sqrt(") && expr.endsWith(")")) {
            double val = Double.parseDouble(expr.substring(5, expr.length() - 1));
            return BigDecimal.valueOf(Math.sqrt(val)).setScale(precision, RoundingMode.HALF_UP);
        }

        // 处理 abs
        if (expr.startsWith("abs(") && expr.endsWith(")")) {
            double val = Double.parseDouble(expr.substring(4, expr.length() - 1));
            return BigDecimal.valueOf(Math.abs(val)).setScale(precision, RoundingMode.HALF_UP);
        }

        // 处理幂运算 ^
        if (expr.contains("^")) {
            String[] parts = expr.split("\\^");
            double base = Double.parseDouble(parts[0]);
            double exp = Double.parseDouble(parts[1]);
            return BigDecimal.valueOf(Math.pow(base, exp)).setScale(precision, RoundingMode.HALF_UP);
        }

        // 处理乘除
        if (expr.contains("*") || expr.contains("/")) {
            if (expr.contains("*")) {
                String[] parts = expr.split("\\*");
                double a = Double.parseDouble(parts[0]);
                double b = Double.parseDouble(parts[1]);
                return BigDecimal.valueOf(a * b).setScale(precision, RoundingMode.HALF_UP);
            } else {
                String[] parts = expr.split("/");
                double a = Double.parseDouble(parts[0]);
                double b = Double.parseDouble(parts[1]);
                return BigDecimal.valueOf(a / b).setScale(precision, RoundingMode.HALF_UP);
            }
        }

        // 处理加减
        if (expr.contains("+")) {
            String[] parts = expr.split("\\+");
            double a = Double.parseDouble(parts[0]);
            double b = Double.parseDouble(parts[1]);
            return BigDecimal.valueOf(a + b).setScale(precision, RoundingMode.HALF_UP);
        } else if (expr.contains("-") && expr.indexOf("-") > 0) {
            String[] parts = expr.split("-", 2);
            double a = Double.parseDouble(parts[0]);
            double b = Double.parseDouble(parts[1]);
            return BigDecimal.valueOf(a - b).setScale(precision, RoundingMode.HALF_UP);
        }

        // 纯数字
        return BigDecimal.valueOf(Double.parseDouble(expr)).setScale(precision, RoundingMode.HALF_UP);
    }

    private double getExchangeRate(String from, String to) {
        if (from.equals(to)) return 1.0;
        // 模拟汇率（实际项目中应从 API 获取）
        Map<String, Double> ratesToUSD = Map.of(
                "USD", 1.0,
                "CNY", 7.25,
                "EUR", 0.92,
                "JPY", 149.50
        );
        return ratesToUSD.get(to) / ratesToUSD.get(from);
    }
}
