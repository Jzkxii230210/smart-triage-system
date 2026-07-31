package com.zjsru.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicalRecord {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private String doctorName;
    private String diagnosis;
    private String prescription;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long appointmentId;
}
