package com.zjsru.controller;

import com.zjsru.entity.User;
import com.zjsru.service.UserService;
import com.zjsru.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String u = params.get("username");
        String p = params.get("password");

        User user = userService.login(u, p);
        Map<String, Object> res = new HashMap<>();

        if (user != null) {
            Integer specificId = userService.getSpecificId(u);
            String token = JwtUtil.generateToken(user.getId(), u);

            res.put("code", 200);
            res.put("msg", "登录成功");

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            userInfo.put("specificId", specificId);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userInfo", userInfo);

            res.put("data", data);
        } else {
            res.put("code", 400);
            res.put("msg", "用户名或密码错误");
        }
        return res;
    }
}