package com.example.base.common;

/**
 * 业务异常：预期内的失败（如「用户不存在」「状态不允许变更」）。
 *
 * 与「系统异常」分开的意义：
 *   ① 业务异常只返回可读文案，不打印完整堆栈，日志不会被刷屏
 *   ② 系统异常兜底会打 ERROR 堆栈并返回通用文案，避免泄露内部实现
 *
 * 用法：throw new BizException(400, "手机号已被占用");
 * 由 core 的 GlobalExceptionHandler 统一转成 R.fail(...)。
 */
public class BizException extends RuntimeException {

    /** 业务码，沿用 HTTP 语义：400 参数 / 403 无权限 / 404 不存在 / 409 冲突 */
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /** 常见场景快捷构造：默认 400 */
    public BizException(String message) {
        this(400, message);
    }

    public int getCode() {
        return code;
    }
}
