package com.huang.redblack.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.huang.redblack.commen.constant.ExceptionConstant;
import com.huang.redblack.commen.constant.MessageConstant;
import com.huang.redblack.commen.constant.TimeConstant;
import com.huang.redblack.commen.constant.UserAuthLogsConstant;
import com.huang.redblack.commen.exception.BusinessException;
import com.huang.redblack.commen.key.RedisKey;
import com.huang.redblack.mapper.LoginMapper;
import com.huang.redblack.mapper.UserAuthLogsMapper;
import com.huang.redblack.mapper.UsersMapper;
import com.huang.redblack.pojo.dto.UserDTO;
import com.huang.redblack.pojo.entity.UserAuthLogs;
import com.huang.redblack.pojo.entity.Users;
import com.huang.redblack.pojo.vo.TokenVO;
import com.huang.redblack.service.LoginService;
import com.huang.redblack.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private TokenService tokenService;


    @Override
    @Operation(summary = "邮箱登录")
    public TokenVO loginByEmail(UserDTO userDTO) {
        String email = userDTO.getEmail();
        Users user = lambdaQuery().eq(Users::getEmail, email).one();
        if (Objects.isNull(user)) {
            throw new BusinessException(ExceptionConstant.NOT_FOUND, ExceptionConstant.MSG_USER_NOT_FOUND);
        }
        return tokenService.login(user.getEmail());
    }



    @Override
    @Operation(summary = "密码登录")
    public String loginByPassword(UserDTO userDTO) {
        // TODO 密码登录

        return null;
    }

    @Override
    @Operation(summary = "注册")
    public TokenVO register(UserDTO userDTO) {
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
                throw new BusinessException(ExceptionConstant.BAD_REQUEST, MessageConstant.USERNAME_OR_PASSWORD_ERROR);
            }

            // 2. 校验验证码
            String codeKey = RedisKey.USER + userDTO.getEmail() + ":" + RedisKey.VERIFY_CODE;
            String redisCode = redisTemplate.opsForValue().get(codeKey);

            if (redisCode == null) {
                throw new BusinessException(ExceptionConstant.BAD_REQUEST, MessageConstant.VERIFY_CODE_EXPIRED);
            }
            if (!verifyCode.equals(redisCode)) {
                throw new BusinessException(ExceptionConstant.BAD_REQUEST, MessageConstant.VERIFY_CODE_ERROR);
            }
            // 3. 校验通过后立即删除验证码（一次性使用）
            redisTemplate.delete(codeKey);

            //1.1 邮箱格式检查
            if(!email.matches(EMAIL_REGEX)){
                throw new BusinessException(ExceptionConstant.BAD_REQUEST, MessageConstant.EMAIL_FORMAT_ERROR);
            }

            // 4. 校验邮箱是否已注册
            Users existUser = lambdaQuery()
                    .eq(Users::getEmail, email)
                    .one();
            if (existUser != null) {
                throw new BusinessException(ExceptionConstant.CONFLICT, MessageConstant.EMAIL_ALREADY_EXISTS);
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

            // 6. 返回 VO
            TokenVO tokenVO = tokenService.login(newUser.getEmail());

            //保存注册成功信息
            userAuthLogs.setResult(1);
            userAuthLogsMapper.insert(userAuthLogs);
            return tokenVO;
        } catch (RuntimeException e) {
            userAuthLogs.setResult(0);
            userAuthLogs.setFail_reason(e.getMessage());
            userAuthLogsMapper.insert(userAuthLogs);
            throw e;
        }
    }


    @Override
    @Operation(summary = "发送验证码")
    public String sendCode(UserDTO userDTO) {
        //1.1 邮箱格式检查
        String email = userDTO.getEmail();
        if(!email.matches(EMAIL_REGEX)){
            throw new BusinessException(ExceptionConstant.BAD_REQUEST, MessageConstant.EMAIL_FORMAT_ERROR);
        }
        //1.2 邮箱是否已存在
        Users user = lambdaQuery().eq(Users::getEmail, email).one();
        if(Objects.nonNull(user)){
            throw new BusinessException(ExceptionConstant.CONFLICT, MessageConstant.EMAIL_ALREADY_EXISTS);
        }
        //1.3 验证码生成
        StringBuilder verifyCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            verifyCode.append(RANDOM.nextInt(10));
        }
        redisTemplate.opsForValue().set(RedisKey.USER + email + ":" + RedisKey.VERIFY_CODE, verifyCode.toString(), TimeConstant.VERIFY_CODE_EXPIRE_TIME, TimeUnit.MINUTES);
        //TODO 发送验证码
        log.info("已将验证码{}发送到邮箱{}", verifyCode, email);
        return verifyCode.toString();
    }


}




