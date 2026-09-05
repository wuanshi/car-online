package com.caronline.http;

import java.util.Optional;

/**
 * 从 URL 路径里取出资源 id。
 * 例如 /api/passengers/3 → 3；/api/passengers → 空。
 */
public final class Paths {

    private Paths() {
    }

    /**
     * @return 没有 id 段时 empty；有数字 id 时 of(id)
     * @throws IllegalArgumentException id 段不是正整数，或路径多了一层
     */
    public static Optional<Integer> readId(String path, String prefix) {
        String normalized = trimSlash(path);
        String base = trimSlash(prefix);
        if (normalized.equals(base)) {
            return Optional.empty();
        }
        if (!normalized.startsWith(base + "/")) {
            throw new IllegalArgumentException("接口不存在");
        }
        String rest = normalized.substring(base.length() + 1);
        if (rest.isEmpty() || rest.contains("/")) {
            throw new IllegalArgumentException("接口不存在");
        }
        try {
            int id = Integer.parseInt(rest);
            if (id <= 0) {
                throw new IllegalArgumentException("id 必须是正整数");
            }
            return Optional.of(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id 必须是数字");
        }
    }

    /**
     * 解析 /资源、/资源/{id}、/资源/{id}/动作。
     */
    public static ParsedPath parse(String path, String prefix) {
        String normalized = trimSlash(path);
        String base = trimSlash(prefix);
        if (normalized.equals(base)) {
            return new ParsedPath(Optional.empty(), Optional.empty());
        }
        if (!normalized.startsWith(base + "/")) {
            throw new IllegalArgumentException("接口不存在");
        }
        String rest = normalized.substring(base.length() + 1);
        String[] parts = rest.split("/");
        if (parts.length == 0 || parts.length > 2) {
            throw new IllegalArgumentException("接口不存在");
        }
        Optional<Integer> id = parsePositiveId(parts[0]);
        Optional<String> action = parts.length == 2 ? Optional.of(parts[1]) : Optional.empty();
        return new ParsedPath(id, action);
    }

    public static String queryParam(String query, String name) {
        if (query == null || query.isBlank()) {
            return null;
        }
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && name.equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }

    private static Optional<Integer> parsePositiveId(String raw) {
        try {
            int id = Integer.parseInt(raw);
            if (id <= 0) {
                throw new IllegalArgumentException("id 必须是正整数");
            }
            return Optional.of(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id 必须是数字");
        }
    }

    public static final class ParsedPath {
        private final Optional<Integer> id;
        private final Optional<String> action;

        private ParsedPath(Optional<Integer> id, Optional<String> action) {
            this.id = id;
            this.action = action;
        }

        public Optional<Integer> id() {
            return id;
        }

        public Optional<String> action() {
            return action;
        }
    }

    private static String trimSlash(String value) {
        if (value != null && value.length() > 1 && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
