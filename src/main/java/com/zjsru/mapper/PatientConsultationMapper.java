package com.zjsru.mapper;

import com.zjsru.entity.Consultation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PatientConsultationMapper {
    // 根据患者ID查询列表
    List<Consultation> selectListByPatientId(Integer patientId);

    // 查询所有医生
    List<Map<String, Object>> selectAllDoctors();

    // 根据ID查询单个
    Consultation selectById(@Param("id") Integer id);

    // 插入新咨询
    int insert(Consultation consultation);

    // 更新状态
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

     // 更新预约状态
     int updateAppointmentStatus(@Param("appointmentId") Integer appointmentId, @Param("status") Integer status);

}
