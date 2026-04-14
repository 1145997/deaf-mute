package dev.forint.deafmute.modules.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserListVO {

    private Long id;

    private String openid;

    private String nickname;

    private String avatar;

    private String realName;

    private String studentNo;

    private String phone;

    private String email;

    private Integer gender;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;
}