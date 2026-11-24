package com.example.base.modules.user.controller;

import com.example.base.common.PageResult;
import com.example.base.common.R;
import com.example.base.modules.user.dto.UserQuery;
import com.example.base.modules.user.service.UserService;
import com.example.base.modules.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户列表接口。
 *
 * 分层职责：Controller 只做「接参数 → 调 Service → 包响应」，
 * 不写 SQL、不拼业务判断。这样以后换成 RPC 或定时任务触发，业务代码不用改。
 */
@Tag(name = "user", description = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor   // Lombok 为 final 字段生成构造器，即推荐的「构造器注入」
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users?page=1&size=10&keyword=张&status=1
     *
     * @Valid 触发 UserQuery 上的 @Min/@Max 校验；失败由 GlobalExceptionHandler 统一转成 R.fail(400)
     *
     * 返回示例：
     * { "code": 0, "message": "success",
     *   "data": { "items": [...], "total": 23, "page": 1, "size": 10 },
     *   "timestamp": 1761000000000 }
     */
    @Operation(summary = "分页查询用户列表", operationId = "listUsers")
    @GetMapping
    public R<PageResult<UserVO>> list(@Valid UserQuery query) {
        return R.ok(userService.query(query));
    }
}
