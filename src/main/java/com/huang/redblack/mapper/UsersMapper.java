package com.huang.redblack.mapper;

import com.huang.redblack.pojo.entity.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;

/**
* @author Admin
* @description 针对表【users(用户表)】的数据库操作Mapper
* @createDate 2026-11-24 23:01:09
* @Entity com.huang.redblack.pojo.entity.Users
*/
public interface UsersMapper extends BaseMapper<Users> {

}




