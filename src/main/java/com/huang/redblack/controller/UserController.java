package com.huang.redblack.controller;

import com.huang.redblack.commen.result.Result;
import com.huang.redblack.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/qing/user")
@RestController
public class UserController {
    @Autowired
    private UsersService usersService;

}
