package com.huang.redblack.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huang.redblack.pojo.entity.UserAuthLogs;
import com.huang.redblack.service.UserAuthLogsService;
import com.huang.redblack.mapper.UserAuthLogsMapper;
import org.springframework.stereotype.Service;

/**
* @author Admin
* @description 针对表【user_auth_logs(认证日志（登录/注册/改密）)】的数据库操作Service实现
* @createDate 2026-11-26 10:37:43
*/
@Service
public class UserAuthLogsServiceImpl extends ServiceImpl<UserAuthLogsMapper, UserAuthLogs>
    implements UserAuthLogsService{

}




