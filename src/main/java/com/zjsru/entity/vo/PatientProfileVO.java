package com.zjsru.entity.vo;

import lombok.Data;
import java.util.List;

@Data
public class PatientProfileVO {
    // 患者基本信息
    private Long patientId;
    private String name;
    private String gender;
    private Integer age;
    private String phone;

    // 关联数据
    private List<AppointmentVO> appointments; // 预约记录列表
    private List<MedicalRecordVO> medicalRecords; // 病历记录列表
}