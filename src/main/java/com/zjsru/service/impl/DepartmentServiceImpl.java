package com.zjsru.service.impl;

import com.zjsru.entity.Department;
import com.zjsru.mapper.DepartmentMapper;
import com.zjsru.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<Department> findAll() {
        return departmentMapper.selectAll();
    }

    @Override
    public void add(Department dept) {
        departmentMapper.insert(dept);
    }

    @Override
    public void update(Department dept) {
        departmentMapper.update(dept);
    }

    @Override
    public void deleteById(Integer id) {
        departmentMapper.deleteById(id);
    }
}