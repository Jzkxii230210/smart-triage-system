package com.zjsru.controller;

import com.zjsru.entity.MedicalRecord;
import com.zjsru.entity.Result;
import com.zjsru.entity.dto.AppointmentStatusDTO;
import com.zjsru.entity.vo.PatientListVO;
import com.zjsru.entity.vo.PatientProfileVO;
import com.zjsru.service.MedicalRecordService;
import com.zjsru.service.PatientManagerService;
import com.zjsru.service.UserService;
import com.zjsru.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/doctor/patient-manager")
public class PatientManagerController {

    @Autowired
    private PatientManagerService patientManagerService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private UserService userService;

    @GetMapping("/profile/{id}")
    public Result<PatientProfileVO> getPatientProfile(@PathVariable Long id, HttpServletRequest request) {
        // 1. 获取当前登录医生ID
        String token = request.getHeader("Authorization");
        String username = JwtUtil.getUsernameFromToken(token);
        Integer doctorId = userService.getSpecificId(username);

        // 2. 传入 doctorId 进行查询
        PatientProfileVO profile = patientManagerService.getPatientProfile(id, doctorId);
        return Result.success(profile);
    }

    // 完成就诊并创建病历记录
    @PostMapping("/appointment/status")
    public Result<String> updateAppointmentStatus(@RequestBody AppointmentStatusDTO dto, HttpServletRequest request) {
        try {
            // 1. 获取当前操作的医生ID
            String token = request.getHeader("Authorization");
            String username = JwtUtil.getUsernameFromToken(token);
            Integer doctorId = userService.getSpecificId(username);

            // 2. 将医生ID填入DTO，传递给Service
            dto.setDoctorId(doctorId);

            // 3. 调用Service（统一处理状态更新 + 病历创建）
            patientManagerService.updateAppointmentStatus(dto);

            return Result.success("操作成功");
        } catch (Exception e) {
            log.error("更新预约状态失败: {}", e.getMessage(), e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<PatientListVO>> getPatientList(@RequestParam(required = false) String keyword, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) return Result.error("未登录");
        String username = JwtUtil.getUsernameFromToken(token);
        Integer doctorId = userService.getSpecificId(username);
        List<PatientListVO> list = patientManagerService.getDoctorRelatedPatients(doctorId, keyword);
        return Result.success(list);
    }

    @DeleteMapping("/delete/{patientId}")
    public Result deletePatient(@PathVariable Long patientId) {
        patientManagerService.deletePatientCascade(patientId);
        return Result.success("删除成功");
    }

    @PutMapping("/medical-record/{id}")
    public Result<String> updateMedicalRecord(@PathVariable Long id, @RequestBody MedicalRecord record, HttpServletRequest request) {
        try {
            // 简单校验权限
            String token = request.getHeader("Authorization");
            if (token == null) return Result.error("未登录");

            record.setId(id);
            // 调用 Service 更新
            medicalRecordService.updateMedicalRecord(record);
            return Result.success("病历更新成功");
        } catch (Exception e) {
            log.error("更新病历失败: {}", e.getMessage(), e);
            return Result.error("病历更新失败: " + e.getMessage());
        }
    }
}
