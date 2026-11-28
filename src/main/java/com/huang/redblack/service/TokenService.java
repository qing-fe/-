package com.huang.redblack.service;

import com.huang.redblack.pojo.vo.TokenVO;

public interface TokenService {
    TokenVO login(String email);

    TokenVO refresh(String refreshToken, String oldAccessToken);

    void logout(String at, String refreshToken);
}
