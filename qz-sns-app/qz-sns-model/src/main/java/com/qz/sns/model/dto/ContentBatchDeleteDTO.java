package com.qz.sns.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 批量内容删除请求DTO
 */
@Data
public class ContentBatchDeleteDTO {

    @NotEmpty(message = "内容ID列表不能为空")
    @Size(max = 100, message = "批量删除数量不能超过100")
    private Long[] ids;

    /**
     * 删除原因（可选）
     */
    @Size(max = 500, message = "删除原因不能超过500个字符")
    private String deleteReason;
}