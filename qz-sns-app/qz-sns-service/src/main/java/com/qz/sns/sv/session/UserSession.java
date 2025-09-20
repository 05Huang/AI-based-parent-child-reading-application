package com.qz.sns.sv.session;

import com.qz.sns.model.models.IMSessionInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserSession extends IMSessionInfo {

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户类型
     */
    private Integer userType;
}
