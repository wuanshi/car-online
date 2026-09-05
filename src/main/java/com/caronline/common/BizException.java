package com.caronline.common;

/**
 * 业务规则失败：重复手机号、非法状态跳转等。
 * Handler 把它转成 code != 0 的 JSON，不把堆栈给前端。
 */
public class BizException extends RuntimeException {

    private final int httpStatus;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
