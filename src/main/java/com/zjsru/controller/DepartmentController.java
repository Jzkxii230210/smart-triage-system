package com.zjsru.controller;

import com.zjsru.entity.Result;
import com.zjsru.entity.Department;
import com.zjsru.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dept")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/list")
    public Result<List<Department>> list() {
        return Result.success(departmentService.findAll());
    }

    @PostMapping("/save")
    public Result<?> save(@RequestBody Department dept) {
        if (dept.getId() == null) {
            departmentService.add(dept);
        } else {
            departmentService.update(dept);
        }
        return Result.success("保存成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        departmentService.deleteById(id);
        return Result.success("删除成功");
    }
}