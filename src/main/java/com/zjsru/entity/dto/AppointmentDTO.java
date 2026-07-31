package com.zjsru.entity.dto;

import lombok.Data;

@Data
public class AppointmentDTO {
    private Integer doctorId;
    private Integer patientId;
    private String realName;
    private String title;
    private String deptId;
    private String deptName;
    private String appointmentDate;
    private String timeSlot;
    private String slot;
}

