package com.qz.sns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 内容表
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Data
@ApiModel(value = "Content对象", description = "内容表")
public class Content implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("富文本内容")
    private String content;

    @ApiModelProperty("类型 1:图文 2:视频")
    private Integer type;

    @ApiModelProperty("封面图URL")
    private String coverUrl;

    @ApiModelProperty("媒体URL")
    private String mediaUrl;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("标签，逗号分隔")
    private String tags;

    @ApiModelProperty("浏览数")
    private Integer viewCount;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("评论数")
    private Integer commentCount;

    @ApiModelProperty("分享数")
    private Integer shareCount;

    @ApiModelProperty("状态 1:正常 0:下架")
    private Integer status;

    @ApiModelProperty("创建者ID")
    private Long creatorId;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private BigDecimal clickThroughRate;

    private Integer avgViewDuration;

    private BigDecimal completionRate;

    private BigDecimal popularityScore;

    private Integer recommendedCount;

    private LocalDateTime lastRecommended;

    public void setLastRecommended(LocalDateTime now) {
        if (now != null) {
            lastRecommended = now;
        } else {
            this.lastRecommended = null;
        }
    }
}
