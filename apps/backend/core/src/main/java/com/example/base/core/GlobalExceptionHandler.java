package com.example.base.core;

import com.example.base.common.BizException;
import com.example.base.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把散落在各 Controller 的 try-catch 收敛到一处。
 *
 * 没有这一层时，异常会返回 Spring 默认的错误 JSON（timestamp / status / error / path），
 * 前端的 axios 拦截器就得兼容两套结构；有了它，前端永远只解析 R。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验失败（@Valid 作用于 GET 查询对象时抛 BindException，
     * 作用于 @RequestBody 时抛 MethodArgumentNotValidException，两种都接住）
     */
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public R<Void> handleValidation(Exception e) {
        // 只把第一条错误信息返回，避免把一堆字段错误糊到页面上
        String msg = "参数不合法";
        if (e instanceof BindException be && be.getBindingResult().hasFieldErrors()) {
            msg = be.getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof MethodArgumentNotValidException me
                && me.getBindingResult().hasFieldErrors()) {
            msg = me.getBindingResult().getFieldError().getDefaultMessage();
        }
        log.warn("参数校验失败：{}", msg);
        return R.fail(400, msg);
    }

    /**
     * 预期内的业务失败：只记一行 warn，不打堆栈（业务异常刷 ERROR 会淹没真正的故障）。
     * 新增业务模块时直接 throw new BizException(code, msg) 即可，不需要在 Controller 里 try-catch。
     */
    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e) {
        log.warn("业务失败[{}]：{}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 兜底异常：记录完整堆栈（排查靠日志），但绝不把堆栈内容返回给前端 —— 会泄露内部实现。
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleUnexpected(Exception e) {
        log.error("接口异常", e);
        return R.fail(500, "服务开小差了，请稍后重试");
    }
}
