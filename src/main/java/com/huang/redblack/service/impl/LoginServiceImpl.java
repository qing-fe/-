package com.huang.redblack.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.huang.redblack.commen.constant.MessageConstant;
import com.huang.redblack.commen.constant.TimeConstant;
import com.huang.redblack.commen.constant.UserAuthLogsConstant;
import com.huang.redblack.commen.key.RedisKey;
import com.huang.redblack.commen.utils.JWTUtil;
import com.huang.redblack.mapper.LoginMapper;
import com.huang.redblack.mapper.UserAuthLogsMapper;
import com.huang.redblack.mapper.UsersMapper;
import com.huang.redblack.pojo.dto.UserDTO;
import com.huang.redblack.pojo.entity.UserAuthLogs;
import com.huang.redblack.pojo.entity.Users;
import com.huang.redblack.pojo.vo.LoginVO;
import com.huang.redblack.pojo.vo.UserVO;
import com.huang.redblack.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.mbeans.UserMBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

/**
 * @author Admin
 * @description 针对表【users(用户表)】的数据库操作Service实现
 * @createDate 2026-11-24 22:46:56
 */
@Service
@Slf4j
public class LoginServiceImpl extends ServiceImpl<UsersMapper, Users> implements LoginService {
    private static final String EMAIL_REGEX = "^[0-9]{13}@stu\\.hbnu\\.edu\\.cn$"; //邮箱校验格式
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private LoginMapper loginMapper;
    @Autowired
    private UserAuthLogsMapper userAuthLogsMapper;


    @Override
    @Operation(summary = "邮箱登录")
    public String loginByEmail(UserDTO userDTO) {
        String email = userDTO.getEmail();
        Users user = lambdaQuery().eq(Users::getEmail, email).one();
        if(Objects.isNull(user)){
            //TODO 用户不存在
        }
        //TODO 用户存在，完成登录
        String token = JWTUtil.generateToken(user.getId().longValue(), user.getEmail());
        return token;
    }



    @Override
    @Operation(summary = "密码登录")
    public String loginByPassword(UserDTO userDTO) {
        // TODO 密码登录
        return null;
    }

    @Override
    @Operation(summary = "注册")
    public UserVO register(UserDTO userDTO) {
        String email = userDTO.getEmail();
        String verifyCode = userDTO.getVerifyCode();
        //日志
        UserAuthLogs userAuthLogs = new UserAuthLogs();
        userAuthLogs.setEmail(email);
        userAuthLogs.setOperation(UserAuthLogsConstant.REGISTER);
        userAuthLogs.setCreated_at(LocalDateTime.now());
        try {
            // 1. 基础判空
            if (email == null || verifyCode == null) {
                userAuthLogs.setResult(0);
                userAuthLogs.setFail_reason(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
                userAuthLogsMapper.insert(userAuthLogs);
                throw new RuntimeException(MessageConstant.USERNAME_OR_PASSWORD_ERROR);
            }

            // 2. 校验验证码
            String codeKey = RedisKey.USER_VERIFY_CODE + email;
            String redisCode = redisTemplate.opsForValue().get(codeKey);

            if (redisCode == null) {
                throw new RuntimeException(MessageConstant.VERIFY_CODE_EXPIRED);
            }
            if (!verifyCode.equals(redisCode)) {
                throw new RuntimeException(MessageConstant.VERIFY_CODE_ERROR);
            }
            // 3. 校验通过后立即删除验证码（一次性使用）
            redisTemplate.delete(codeKey);

            //1.1 邮箱格式检查
            if(!email.matches(EMAIL_REGEX)){
                throw new RuntimeException(MessageConstant.EMAIL_FORMAT_ERROR);
            }

            // 4. 校验邮箱是否已注册
            Users existUser = lambdaQuery()
                    .eq(Users::getEmail, email)
                    .one();
            if (existUser != null) {
                throw new RuntimeException(MessageConstant.EMAIL_ALREADY_EXISTS);
            }

            // 5. 创建用户（不用 BeanUtils 复制密码，手动设值更安全）
            Users newUser = new Users();

            // 加密
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String hash = encoder.encode(userDTO.getPassword());

            BeanUtil.copyProperties(userDTO, newUser);
            newUser.setPassword_hash(hash);
            newUser.setCreated_at(LocalDateTime.now());
            newUser.setUpdated_at(LocalDateTime.now());
            save(newUser);

            //保存用户信息
            loginMapper.insert(newUser);

            // 6. 返回 VO
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(newUser, vo);

            //保存注册成功信息
            userAuthLogs.setResult(1);
            userAuthLogsMapper.insert(userAuthLogs);
            return vo;
        } catch (RuntimeException e) {
            userAuthLogs.setResult(0);
            userAuthLogs.setFail_reason(e.getMessage());
            userAuthLogsMapper.insert(userAuthLogs);
            throw new RuntimeException(e);
        }
    }


    @Override
    @Operation(summary = "发送验证码")
    public String sendCode(UserDTO userDTO) {
        //1.1 邮箱格式检查
        String email = userDTO.getEmail();
        if(!email.matches(EMAIL_REGEX)){
            throw new RuntimeException(MessageConstant.EMAIL_FORMAT_ERROR);
        }
        //1.2 邮箱是否已存在
        Users user = lambdaQuery().eq(Users::getEmail, email).one();
        if(Objects.nonNull(user)){
            throw new RuntimeException(MessageConstant.EMAIL_ALREADY_EXISTS);
        }
        //1.3 验证码生成
        StringBuilder verifyCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            verifyCode.append(RANDOM.nextInt(10));
        }
        redisTemplate.opsForValue().set(RedisKey.USER_VERIFY_CODE + email, verifyCode.toString(), TimeConstant.VERIFY_CODE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.info("已将验证码{}发送到邮箱{}", verifyCode, email);
        return verifyCode.toString();
    }


}




