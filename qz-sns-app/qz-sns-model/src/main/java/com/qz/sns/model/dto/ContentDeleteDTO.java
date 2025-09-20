package com.qz.sns.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContentDeleteDTO {

    @NotNull(message = "内容ID不能为空")
    private Long id;

    /**
     * 删除原因（可选）
     */
    @Size(max = 500, message = "删除原因不能超过500个字符")
    private String deleteReason;
}