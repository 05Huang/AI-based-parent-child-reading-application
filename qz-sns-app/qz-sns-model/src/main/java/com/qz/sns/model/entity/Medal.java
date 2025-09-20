package com.qz.sns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 勋章表
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@ApiModel(value = "Medal对象", description = "勋章表")
public class Medal implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("勋章名称")
    private String name;

    @ApiModelProperty("勋章描述")
    private String description;

    @ApiModelProperty("勋章图标URL")
    private String icon;

    @ApiModelProperty("获得条件")
    private String condition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "Medal{" +
        "id = " + id +
        ", name = " + name +
        ", description = " + description +
        ", icon = " + icon +
        ", condition = " + condition +
        "}";
    }
}
