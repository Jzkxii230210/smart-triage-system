package com.zjsru.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MedicalRecordVO {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private String realName;
    private String diagnosis;
    private String prescription;
    private LocalDateTime createTime;
    private Long appointmentId;
    private LocalDateTime updateTime;
}
