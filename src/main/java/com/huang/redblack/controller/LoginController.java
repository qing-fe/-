package com.huang.redblack.controller;

import com.huang.redblack.commen.result.Result;
import com.huang.redblack.pojo.dto.UserDTO;
import com.huang.redblack.pojo.vo.LoginVO;
import com.huang.redblack.pojo.vo.UserVO;
import com.huang.redblack.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.el.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/qing/login")
@Tag(name = "登录管理" , description = "登录相关接口管理")
public class LoginController {
    @Autowired
    private LoginService loginService;

    @PostMapping("/email")
    @Operation(summary = "邮箱登录", description = "通过邮箱登录系统")
    public Result<String> loginByEmail(@RequestBody UserDTO userDTO){
        String token = loginService.loginByEmail(userDTO);
        log.info("token: {}", token);
        return Result.success(token);
    }
    /*{
        "username": "huang",
            "password": "327079254",
            "email": "327079254@qq.com"
    }*/

    @PostMapping("/password")
    @Operation(summary = "密码登录", description = "通过密码登录系统")
    public Result<String> loginByPassword(@RequestBody UserDTO userDTO){
        String token = loginService.loginByPassword(userDTO);
        log.info("token: {}", token);
        return Result.success(token);
    }

    @PostMapping("/register")
    @Operation(summary = "注册", description = "注册系统")
    public Result<UserVO> register(@RequestBody UserDTO userDTO){
        UserVO user = loginService.register(userDTO);
        log.info("userVO: {}", user);
        return Result.success(user);
    }

    @PostMapping("/sendCode")
    @Operation(summary = "发送验证码", description = "通过邮箱发送验证码")
    public Result<String> sendCode(@RequestBody UserDTO userDTO){
        String code = loginService.sendCode(userDTO);
        log.info("code: {}", code);
        return Result.success(code);
    }

}
