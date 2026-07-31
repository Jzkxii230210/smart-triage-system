package com.zjsru.controller;

import com.zjsru.entity.Result;
import com.zjsru.entity.dto.EvaluateDTO;
import com.zjsru.entity.dto.UserUpdateDTO;
import com.zjsru.entity.vo.AppointmentVO;
import com.zjsru.entity.vo.ProfileVO;
import com.zjsru.service.UserService;
import com.zjsru.service.impl.ProfileServiceImpl;
import com.zjsru.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patient/profile")
public class PatientProfileController {

    @Autowired
    private ProfileServiceImpl profileService;

    @Autowired
    private UserService userService;

    private Integer getCurrentPatientId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String username = JwtUtil.getUsernameFromToken(token);
        return userService.getSpecificId(username);
    }

    @GetMapping("/info")
    public Result<ProfileVO> getInfo(HttpServletRequest request) {
        try {
            Integer patientId = getCurrentPatientId(request);
            return Result.success(profileService.getUserInfo(patientId));
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public Result<String> updateInfo(@RequestBody UserUpdateDTO dto, HttpServletRequest request) {
        try {
            Integer patientId = getCurrentPatientId(request);
            profileService.updateInfo(patientId, dto);
            return Result.success("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @GetMapping("/appointments")
    public Result<List<AppointmentVO>> getAppointments(HttpServletRequest request) {
        try {
            Integer patientId = getCurrentPatientId(request);
            return Result.success(profileService.getMyRecords(patientId));
        } catch (Exception e) {
            return Result.error("获取记录失败");
        }
    }

    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable("id") Integer id) {
        try {
            if (id == null) return Result.error("参数错误");
            profileService.cancelBooking(id);
            return Result.success("预约已取消");
        } catch (Exception e) {
            return Result.error("系统异常: " + e.getMessage());
        }
    }

    @PostMapping("/evaluate")
    public Result<String> evaluate(@RequestBody EvaluateDTO dto) {
        try {
            profileService.evaluate(dto);
            return Result.success("评价提交成功");
        } catch (Exception e) {
            return Result.error("评价提交失败: " + e.getMessage());
        }
    }
}