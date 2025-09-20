package com.qz.sns.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类创建请求DTO
 */
@Data
public class CategoryCreateDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称长度不能超过64个字符")
    private String name;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sortOrder = 0;
}