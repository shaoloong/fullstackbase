package com.example.base.modules.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 修改产品入参：所有字段均可空，null 表示「不改」；
 * 全部字段都为 null 时由服务层抛 BizException(400, "没有可更新的字段")。
 */
@Data
public class ProductUpdateReq {

    @Size(min = 2, max = 128, message = "产品名称长度在 2 到 128 个字符之间")
    private String name;

    @Size(max = 64, message = "产品编码最多 64 个字符")
    @Pattern(regexp = ".*\\S.*", message = "产品编码必须包含非空白字符")
    private String code;

    @Size(max = 64, message = "产品分类最多 64 个字符")
    private String category;

    @DecimalMin(value = "0.01", message = "售价不能小于 0.01")
    private BigDecimal price;

    @Min(value = 0, message = "库存不能小于 0")
    private Integer stock;

    @Size(max = 500, message = "产品描述最多 500 个字符")
    private String description;

    /** 1 启用 / 0 停用；null 表示不改 */
    @Min(value = 0, message = "状态只能是 0（停用）或 1（启用）")
    @Max(value = 1, message = "状态只能是 0（停用）或 1（启用）")
    private Integer status;
}
