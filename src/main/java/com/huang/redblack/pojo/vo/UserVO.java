package com.huang.redblack.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    private String username;
    private String password;
    private String email;
    private String verifyCode; // 验证码
    private String avatarUrl; // 头像地址
}
