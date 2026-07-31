package com.zjsru.controller;

import com.zjsru.entity.Result;
import com.zjsru.entity.dto.AppointmentDTO;
import com.zjsru.entity.vo.AppointmentVO;
import com.zjsru.service.AppointmentService;
import com.zjsru.service.UserService;
import com.zjsru.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient/appointment")
public class PatientAppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    // 获取科室
    @GetMapping("/depts")
    public Result<List<Map<String, Object>>> getDepts() {
        return Result.success(appointmentService.getDeptList());
    }

    // 获取医生排班
    @GetMapping("/doctors")
    public Result<List<AppointmentVO>> getDoctors(@RequestParam(value = "deptName", defaultValue = "0") String deptName, @RequestParam(value = "date") String date) {
        return Result.success(appointmentService.getDoctorList(deptName, date));
    }

    // 提交预约
    @PostMapping("")
    public Result<String> createAppointment(@RequestBody AppointmentDTO dto, HttpServletRequest request) {
        // 1. 解析 Token 获取用户名
        String token = request.getHeader("Authorization");
        String username = JwtUtil.getUsernameFromToken(token);

        try {
            // 2. 【关键】调用分流方法，智能获取 ID
            Integer businessId = userService.getSpecificId(username);

            // 3. 传入真实的 ID 进行业务处理
            appointmentService.submitAppointment(dto, businessId);

            return Result.success("预约成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

