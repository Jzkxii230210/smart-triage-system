package com.zjsru.controller;

import com.zjsru.service.ConsultationService;
import com.zjsru.entity.Result;
import com.zjsru.service.UserService;
import com.zjsru.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/doctor/consultation")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result list(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.error("未登录（Token为空）");
        }
        String username = JwtUtil.getUsernameFromToken(token);
        Integer doctorId = userService.getSpecificId(username);
        return Result.success(consultationService.getDoctorConsultations(doctorId));
    }

    @GetMapping("/history/{id}")
    public Result history(@PathVariable Integer id) {
        return Result.success(consultationService.getChatHistory(id));
    }

    @PostMapping("/reply")
    public Result reply(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String content = (String) params.get("content");

        consultationService.sendMessage(id, content, "doctor");
        return Result.success("消息发送成功");
    }

    @PostMapping("/end/{id}")
    public Result endConsultation(@PathVariable Integer id) {
        try {
            consultationService.endConsultation(id);
            return Result.success("会诊已结束");
        } catch (Exception e) {
            return Result.error("结束会诊失败: " + e.getMessage());
        }
    }
}
