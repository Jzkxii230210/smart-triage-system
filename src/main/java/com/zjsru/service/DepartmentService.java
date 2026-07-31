package com.zjsru.service;

import com.zjsru.entity.Department;
import java.util.List;

public interface DepartmentService {
    // 查询所有科室
    List<Department> findAll();

    // 添加科室
    void add(Department dept);

    // 修改科室
    void update(Department dept);

    // 删除科室
    void deleteById(Integer id);
}