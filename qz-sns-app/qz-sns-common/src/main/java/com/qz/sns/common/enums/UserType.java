package com.qz.sns.common.enums;

/**
 * 用户类型枚举
 */
public enum UserType {
    
    /**
     * 普通用户
     */
    NORMAL(1, "普通用户"),
    
    /**
     * 管理员
     */
    ADMIN(2, "管理员");
    
    private final Integer code;
    private final String description;
    
    UserType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据code获取用户类型
     * @param code 用户类型码
     * @return 用户类型枚举
     */
    public static UserType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserType userType : values()) {
            if (userType.getCode().equals(code)) {
                return userType;
            }
        }
        return null;
    }
}