package com.zjsru.mapper;

import com.zjsru.entity.Consultation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ConsultationMapper {
    // 查询医生列表
    List<Consultation> selectListByDoctorId(Integer doctorId);

    // 查询详情
    Consultation selectById(@Param("id") Integer id);

    // 更新状态
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    // 更新会诊对话状态
    int updateConsultationStatus(@Param("appointmentId") Long appointmentId, @Param("status") Integer status);

    // 查询未结束的会诊对话
    int countActiveConsultation(@Param("doctorId") Integer doctorId, @Param("patientId") Integer patientId, @Param("completedStatus") Long completedStatus);

    // 插入会诊
    int insertConsultation(Consultation consultation);
}