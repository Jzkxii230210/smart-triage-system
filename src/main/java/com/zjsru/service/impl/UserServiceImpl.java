package com.zjsru.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjsru.entity.dto.UserDTO;
import com.zjsru.mapper.DoctorMapper;
import com.zjsru.mapper.PatientManagerMapper;
import com.zjsru.mapper.UserMapper;
import com.zjsru.entity.User;
import com.zjsru.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private PatientManagerMapper patientMapper;

    @Override
    public User login(String username, String password) {
        return userMapper.getByUsernameAndPassword(username, password);
    }

    public Integer getSpecificId(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String role = user.getRole();

        // 2. 根据角色分流查询
        Integer specificId = null;

        if ("doctor".equalsIgnoreCase(role)) {
            specificId = doctorMapper.findIdByUsername(username);
        } else if ("patient".equalsIgnoreCase(role)) {
            specificId = patientMapper.findIdByUsername(username);
        } else if ("admin".equalsIgnoreCase(role)) {
            specificId = user.getId();
        }

        if (specificId == null) {
            throw new RuntimeException("未找到该角色对应的详细信息表记录");
        }

        return specificId;
    }

    @Override
    public PageInfo<UserDTO> getUserList(Integer page, Integer size, String username, String role) {
        PageHelper.startPage(page, size);
        List<UserDTO> list = userMapper.selectUserDTOList(username, role);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional
    public void addUser(User user) {
        if (user.getPassword() == null) {
            user.setPassword("123456");
        }
        userMapper.insertUser(user);

    }

    @Override
    @Transactional
    public void updateUser(User user) {
        User oldUser = userMapper.selectById(user.getId());
        String oldUsername = oldUser.getUsername();

        userMapper.updateUser(user);
        if (!oldUsername.equals(user.getUsername())) {
            userMapper.updateDoctorUsername(oldUsername, user.getUsername());
            userMapper.updatePatientUsername(oldUsername, user.getUsername());
        }
    }

    @Override
    public void deleteUser(Integer id) {
        userMapper.deleteUserById(id);
    }

    @Override
    public void resetPassword(Integer id, String password) {
        userMapper.updatePassword(id, password);
    }

    @Override
    @Transactional
    public void register(User user) throws Exception {
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new Exception("用户名已存在");
        }

        User existingByEmail = userMapper.findByEmail(user.getEmail());
        if (existingByEmail != null) {
            throw new Exception("邮箱已被注册");
        }
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("patient");
        }
        userMapper.insertUser(user);
    }

    @Override
    public User getById(Integer id) {
        return userMapper.selectById(id);
    }

}
