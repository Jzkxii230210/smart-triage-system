package com.zjsru.mapper;

import com.zjsru.entity.dto.AppointmentStatusDTO;
import com.zjsru.entity.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PatientManagerMapper {
    // 获取患者信息
    PatientProfileVO selectPatientBasicInfo(@Param("patientId") Long patientId);

    // 获取患者预约信息
    List<AppointmentVO> selectAppointmentsByPatientId(@Param("patientId") Long patientId, @Param("doctorId") Integer doctorId);

    // 获取患者病历信息
    List<MedicalRecordVO> selectMedicalRecordsByPatientId(@Param("patientId") Long patientId);

    // 获取患者评价信息
    void updateAppointmentStatus(AppointmentStatusDTO dto);

    // 插入患者评价信息
    void insertMedicalRecord(AppointmentStatusDTO dto);

    // 删除患者评价信息
    void deleteMedicalRecordsByPatientId(@Param("patientId") Long patientId);

    // 删除患者预约信息
    void deleteAppointmentsByPatientId(@Param("patientId") Long patientId);

    // 删除患者信息
    int deletePatientById(@Param("patientId") Long patientId);

    // 根据预约ID获取患者ID
    Long getPatientIdByAppointmentId(@Param("appointmentId") Long appointmentId);

    // 根据用户名获取患者ID
    Integer findIdByUsername(String username);

    // 获取医生关联的患者列表
    List<PatientListVO> selectDoctorRelatedPatients(@Param("doctorId") Integer doctorId, @Param("keyword") String keyword);

    // 获取患者关联的医生列表
    Map<String, Object> getAppointmentSimpleInfo(@Param("id") Long id);

    // 获取患者姓名
    String getPatientNameById(@Param("id") Integer id);
}