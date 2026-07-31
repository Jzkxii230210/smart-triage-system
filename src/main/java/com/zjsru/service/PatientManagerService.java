package com.zjsru.service;

import com.zjsru.entity.dto.AppointmentStatusDTO;
import com.zjsru.entity.vo.PatientListVO;
import com.zjsru.entity.vo.PatientProfileVO;
import java.util.List;

public interface PatientManagerService {
    // 查询患者列表
    List<PatientListVO> getDoctorRelatedPatients(Integer doctorId, String keyword);

    // 查询患者详情（含预约和病历）
    PatientProfileVO getPatientProfile(Long patientId, Integer doctorId);

    // 更新预约状态（接诊/完成/取消）
    void updateAppointmentStatus(AppointmentStatusDTO dto);

    // 删除患者
    void deletePatientCascade(Long patientId);

}