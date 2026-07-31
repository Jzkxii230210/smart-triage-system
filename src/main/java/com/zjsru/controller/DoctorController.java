package com.zjsru.controller;

import com.zjsru.entity.Result;
import com.zjsru.entity.Schedule;
import com.zjsru.entity.dto.DoctorDTO;
import com.zjsru.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // 获取列表
    @GetMapping("/list")
    public Result<List<DoctorDTO>> list() {
        return Result.success(doctorService.findAllDoctors());
    }

    // 保存排班
    @PostMapping("/schedule/save")
    public Result<?> saveSchedule(@RequestBody Schedule schedule) {
        doctorService.addSchedule(schedule);
        return Result.success("排班成功");
    }

    // 取消排班
    @DeleteMapping("/schedule/{id}")
    public Result<?> deleteSchedule(@PathVariable Integer id) {
        doctorService.removeSchedule(id);
        return Result.success("取消排班成功");
    }
}