package com.zjsru.service;

import com.github.pagehelper.PageInfo;
import com.zjsru.entity.User;
import com.zjsru.entity.dto.UserDTO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    User login(String username, String password);
    // 分页查询列表
    PageInfo<UserDTO> getUserList(Integer page, Integer size, String username, String role);

    // 新增用户
    void addUser(User user);

    // 更新用户
    void updateUser(User user);

    // 删除用户
    void deleteUser(Integer id);

    // 重置密码
    void resetPassword(Integer id, String password);

    // 获取用户ID
    Integer getSpecificId(String username);

    // 注册
    void register(User user) throws Exception;

    // 根据ID获取用户
    User getById(Integer id);
}