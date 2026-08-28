package com.caronline.http;

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
}
