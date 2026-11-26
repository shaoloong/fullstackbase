package com.example.base.modules.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增产品入参。校验失败由 GlobalExceptionHandler 统一转成 R.fail(400, msg)。
 */
@Data
public class ProductCreateReq {

    @NotBlank(message = "产品名称不能为空")
    @Size(min = 2, max = 128, message = "产品名称长度在 2 到 128 个字符之间")
    private String name;

    @NotBlank(message = "产品编码不能为空")
    @Size(max = 64, message = "产品编码最多 64 个字符")
    @Pattern(regexp = ".*\\S.*", message = "产品编码必须包含非空白字符")
    private String code;

    @Size(max = 64, message = "产品分类最多 64 个字符")
    private String category;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价不能小于 0.01")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于 0")
    private Integer stock;

    @Size(max = 500, message = "产品描述最多 500 个字符")
    private String description;

    /** 1 启用 / 0 停用；不传由服务层置为 1 */
    @Min(value = 0, message = "状态只能是 0（停用）或 1（启用）")
    @Max(value = 1, message = "状态只能是 0（停用）或 1（启用）")
    private Integer status;
}
