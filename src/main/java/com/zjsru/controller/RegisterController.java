package com.zjsru.controller;

import com.zjsru.entity.Result;
import com.zjsru.entity.User;
import com.zjsru.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RegisterController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        try {
            // 验证用户输入
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return Result.error("邮箱不能为空");
            }
            if (user.getPassword() == null || user.getPassword().length() < 6) {
                return Result.error("密码长度至少为6位");
            }

            // 执行注册
            userService.register(user);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
