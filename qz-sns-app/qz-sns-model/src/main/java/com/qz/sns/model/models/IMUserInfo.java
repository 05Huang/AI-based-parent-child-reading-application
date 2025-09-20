package com.qz.sns.model.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Blue
 * @date: 2023-09-24 09:23:11
 * @version: 1.0
 */
@Data
@NoArgsConstructor// 用于存储用户信息
@AllArgsConstructor// 用于存储用户信息
public class IMUserInfo {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户终端类型 IMTerminalType
     */
    private Integer terminal;


}
