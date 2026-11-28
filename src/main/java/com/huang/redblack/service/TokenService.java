package com.huang.redblack.service;

import com.huang.redblack.pojo.vo.TokenVO;
import jakarta.servlet.http.HttpServletRequest;

public interface TokenService {
    TokenVO login(String email, HttpServletRequest request);

    TokenVO refresh(String refreshToken, HttpServletRequest request);

    void logout(String at, String refreshToken, HttpServletRequest request);
}
