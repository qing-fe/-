package com.huang.redblack.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.huang.redblack.pojo.entity.Users;
import com.huang.redblack.service.UsersService;
import com.huang.redblack.mapper.UsersMapper;
import org.springframework.stereotype.Service;

/**
* @author Admin
* @description 针对表【users(用户表)】的数据库操作Service实现
* @createDate 2026-11-24 23:01:09
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService{

}




