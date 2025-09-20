package com.qz.sns.model.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String avatarThumb;
    private Integer sex;
    private Integer userType;
    private Integer role;
    private Integer status;
}