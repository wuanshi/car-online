package com.caronline.http;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 极简 JSON：只处理本项目用到的「统一返回」和扁平字符串字段。
 * 故意不引入 Jackson，先看清 HTTP 响应体长什么样。
 */
public final class Json {

    private Json() {
    }

    public static String ok(String dataJson) {
        return "{\"code\":0,\"message\":\"success\",\"data\":" + dataJson + "}";
    }

    public static String fail(String message) {
        return "{\"code\":1,\"message\":" + quote(message) + ",\"data\":null}";
    }

    public static String quote(String text) {
        if (text == null) {
            return "null";
        }
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                + "\"";
    }

    /**
     * 从 {@code {"name":"张三","phone":"138"}} 这种扁平 JSON 里取出字符串字段。
     */
    public static String readString(String json, String key) {
        if (json == null || json.isBlank()) {
            return null;
        }
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    /** 读取数字字段，支持 {@code "id":1} 或 {@code "id":"1"}。 */
    public static Integer readInt(String json, String key) {
        String raw = readNumberToken(json, key);
        if (raw == null) {
            return null;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static BigDecimal readDecimal(String json, String key) {
        String raw = readNumberToken(json, key);
        if (raw == null) {
            return null;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String array(List<String> items) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(items.get(i));
        }
        return builder.append(']').toString();
    }

    private static String readNumberToken(String json, String key) {
        if (json == null || json.isBlank()) {
            return null;
        }
        Pattern number = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = number.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        String quoted = readString(json, key);
        if (quoted == null || quoted.isBlank()) {
            return null;
        }
        return quoted.trim();
    }
}
