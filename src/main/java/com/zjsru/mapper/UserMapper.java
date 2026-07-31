package com.zjsru.mapper;

import com.zjsru.entity.User;
import com.zjsru.entity.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    // 登录
    User getByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    // 获取用户列表
    List<UserDTO> selectUserList(@Param("username") String username, @Param("role") String role);

    // 复杂查询：关联三张表
    List<UserDTO> selectUserDTOList(@Param("username") String username, @Param("role") String role);

    // 新增
    int insertUser(User user);

    // 更新
    int updateUser(User user);

    // 删除
    int deleteUserById(Integer id);

    // 修改密码
    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    // 修改用户名
    int updateDoctorUsername(@Param("oldUsername") String oldUsername, @Param("newUsername") String newUsername);

    // 修改患者用户名
    int updatePatientUsername(@Param("oldUsername") String oldUsername, @Param("newUsername") String newUsername);

    // 根据ID查询
    User selectById(Integer id);

    // 根据用户名查询
    User findByUsername(@Param("username") String username);

    // 根据邮箱查询
    User findByEmail(@Param("email") String email);

}
