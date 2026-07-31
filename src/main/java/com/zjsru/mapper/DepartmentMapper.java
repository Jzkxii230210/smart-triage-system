package com.zjsru.mapper;

import com.zjsru.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DepartmentMapper {
    // 查询所有科室
    List<Department> selectAll();

    // 插入科室
    int insert(Department dept);

    // 修改科室
    int update(Department dept);

    // 删除科室
    int deleteById(Integer id);

    // 查询所有科室名称
    List<String> selectAllDeptNames();
}