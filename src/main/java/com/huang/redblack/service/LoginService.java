package com.huang.redblack.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.huang.redblack.pojo.dto.UserDTO;
import com.huang.redblack.pojo.entity.Users;
import com.huang.redblack.pojo.vo.LoginVO;
import com.huang.redblack.pojo.vo.UserVO;
import org.apache.tomcat.util.buf.UDecoder;

/**
* @author Admin
* @description 针对表【users(用户表)】的数据库操作Service
* @createDate 2026-11-24 22:46:56
*/
public interface LoginService extends IService<Users> {

    /**
     * 邮箱登录
     * @param userDTO
     * @return
     */
    String loginByEmail(UserDTO userDTO);

    /**
     * 密码登录
     * @param userDTO
     * @return
     */
    String loginByPassword(UserDTO userDTO);

    /**
     * 注册
     * @param userDTO
     */
    UserVO register(UserDTO userDTO);

    /**
     * 发送验证码
     * @param userDTO
     * @return
     */
    String sendCode(UserDTO userDTO);
}
