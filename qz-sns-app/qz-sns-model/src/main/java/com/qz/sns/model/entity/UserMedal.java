package com.qz.sns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 用户勋章关系表
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@TableName("user_medal")
@ApiModel(value = "UserMedal对象", description = "用户勋章关系表")
public class UserMedal implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("勋章ID")
    private Long medalId;

    @ApiModelProperty("获得时间")
    private LocalDateTime getTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMedalId() {
        return medalId;
    }

    public void setMedalId(Long medalId) {
        this.medalId = medalId;
    }

    public LocalDateTime getGetTime() {
        return getTime;
    }

    public void setGetTime(LocalDateTime getTime) {
        this.getTime = getTime;
    }

    @Override
    public String toString() {
        return "UserMedal{" +
        "id = " + id +
        ", userId = " + userId +
        ", medalId = " + medalId +
        ", getTime = " + getTime +
        "}";
    }
}
