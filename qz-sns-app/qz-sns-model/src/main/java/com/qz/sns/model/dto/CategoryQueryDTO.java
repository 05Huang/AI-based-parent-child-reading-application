package com.qz.sns.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分类查询请求DTO
 */
@Data
public class CategoryQueryDTO {

    /**
     * 分类名称关键词
     */
    private String name;

    /**
     * 状态：1-正常，0-禁用，null-查询所有状态（默认查询正常状态）
     */
    @Min(value = 0, message = "状态值只能为0或1")
    @Max(value = 1, message = "状态值只能为0或1")
    private Integer status;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer current = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;
}