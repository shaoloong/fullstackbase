package com.example.base.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体：所有接口都返回同一个外壳，前端拦截器只认这一种结构。
 *
 * 成功：{ "code": 0, "message": "success", "data": {...}, "timestamp": 1761000000000 }
 * 失败：{ "code": 400, "message": "参数错误", ... }
 *
 * 为什么不用 HTTP 状态码表达业务结果：
 *   登录过期、余额不足这类「业务失败」用 HTTP 200 + 业务码更好处理，
 *   前端不用为每类业务错误写一套 catch 分支。
 *
 * @param <T> data 的实际类型，泛型让前端能拿到明确的字段提示
 */
@Data
public class R<T> implements Serializable {

    /** 业务码：0 表示成功，非 0 表示失败 */
    private int code;

    /** 给开发/用户看的提示文案 */
    private String message;

    /** 真正的业务数据；失败时通常为 null（已被 Jackson 的 non_null 忽略） */
    private T data;

    /** 服务端时间戳，排查「到底是哪次请求」时很有用 */
    private long timestamp;

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /** 成功响应 */
    public static <T> R<T> ok(T data) {
        return new R<>(0, "success", data);
    }

    /** 失败响应；code 一般直接沿用 HTTP 语义（400 参数、401 未登录、403 无权限、500 服务端） */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }
}
