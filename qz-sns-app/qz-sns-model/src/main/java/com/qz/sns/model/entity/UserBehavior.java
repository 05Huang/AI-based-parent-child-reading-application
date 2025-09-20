package com.qz.sns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户行为实体
 */
@Data
@TableName("user_behavior")
public class UserBehavior {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 行为类型：view,like,comment,share,collect
     */
    private String behaviorType;
    
    /**
     * 行为持续时间(秒)
     */
    private Integer duration;
    
    /**
     * 进度百分比(0-100)
     */
    private BigDecimal progress;
    
    /**
     * 来源：recommend,search,follow,hot等
     */
    private String source;
    
    /**
     * 创建时间
     */
    private Date createdTime;
}