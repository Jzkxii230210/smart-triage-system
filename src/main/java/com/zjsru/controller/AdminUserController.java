package com.zjsru.controller;

import com.github.pagehelper.PageInfo;
import com.zjsru.entity.Result;
import com.zjsru.entity.User;
import com.zjsru.entity.dto.UserDTO;
import com.zjsru.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    // 列表查询
    @GetMapping("/list")
    public Result<PageInfo<UserDTO>> list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size, String username, String role) {
        PageInfo<UserDTO> pageInfo = userService.getUserList(page, size, username, role);
        return Result.success(pageInfo);
    }

    // 新增或更新
    @PostMapping("/save")
    public Result<?> save(@RequestBody User user) {
        if (user.getId() != null) {
            userService.updateUser(user);
        } else {
            userService.addUser(user);
        }
        return Result.success("操作成功");
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    // 重置密码
    @PostMapping("/reset-pwd")
    public Result<?> resetPwd(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String password = (String) params.get("password");
        userService.resetPassword(id, password);
        return Result.success("重置成功");
    }
}