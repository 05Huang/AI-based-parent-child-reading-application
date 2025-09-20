package com.qz.sns.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类更新请求DTO
 */
@Data
public class CategoryUpdateDTO {

    @NotNull(message = "分类ID不能为空")
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称长度不能超过64个字符")
    private String name;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sortOrder;
}