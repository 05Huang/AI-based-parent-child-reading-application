package com.qz.sns.model.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 评论表
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Data
@ApiModel(value = "Comment对象", description = "评论表")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("内容ID")
    private Long contentId;

    @ApiModelProperty("评论用户ID")
    private Long userId;

    @ApiModelProperty("父评论ID")
    private Long parentId;

    @ApiModelProperty("根评论ID")
    private Long rootId;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("回复数")
    private Integer replyCount;

    @ApiModelProperty("状态 1:正常 0:删除")
    private Integer status;

    private LocalDateTime createdTime;

    private Integer commentType;

    private String paragraphId;


}
