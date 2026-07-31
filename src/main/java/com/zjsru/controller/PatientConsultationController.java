package com.zjsru.controller;

import com.zjsru.service.PatientConsultationService;
import com.zjsru.entity.Result;
import com.zjsru.service.UserService;
import com.zjsru.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/patient/consultation")
public class PatientConsultationController {

    @Autowired
    private PatientConsultationService consultationService;

    @Autowired
    private UserService userService;

    private Integer getCurrentPatientId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String username = JwtUtil.getUsernameFromToken(token);
        return userService.getSpecificId(username);
    }

    // 获取列表
    @GetMapping("/list")
    public Result list(HttpServletRequest request) {
        try {
            Integer patientId = getCurrentPatientId(request);
            return Result.success(consultationService.getPatientConsultations(patientId));
        } catch (Exception e) {
            return Result.error("获取咨询列表失败: " + e.getMessage());
        }
    }

    // 获取可选医生列表
    @GetMapping("/doctors")
    public Result getDoctors() {
        return Result.success(consultationService.getDoctorList());
    }

    // 发起新咨询
    @PostMapping("/create")
    public Result create(@RequestBody Map<String, Object> params) {
        Integer patientId = (Integer) params.get("patientId");
        String patientName = (String) params.get("patientName");
        Integer doctorId = (Integer) params.get("doctorId");
        String content = (String) params.get("content");

        consultationService.createConsultation(patientId, patientName, doctorId, content);
        return Result.success("咨询创建成功");
    }

    // 结束会诊
    @PostMapping("/end/{id}")
    public Result endConsultation(@PathVariable Integer id) {
        log.info("收到结束会诊请求，consultationId: {}", id);
        try {
            consultationService.endConsultation(id);
            log.info("会诊结束成功，consultationId: {}", id);
            return Result.success("会诊已结束");
        } catch (Exception e) {
            log.error("结束会诊失败，consultationId: {}, error: {}", id, e.getMessage(), e);
            return Result.error("结束会诊失败: " + e.getMessage());
        }
    }

    // 获取历史
    @GetMapping("/history/{id}")
    public Result history(@PathVariable Integer id) {
        return Result.success(consultationService.getChatHistory(id));
    }

    // 回复/发送消息
    @PostMapping("/reply")
    public Result reply(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String content = (String) params.get("content");

        // 患者端接口，固定身份为 patient
        consultationService.sendMessage(id, content, "patient");
        return Result.success("消息发送成功");
    }
}

