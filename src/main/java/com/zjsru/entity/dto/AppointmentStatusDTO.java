package com.zjsru.entity.dto;

import lombok.Data;


@Data
public class AppointmentStatusDTO {
    private Long appointmentId;
    private Integer status;
    private String diagnosis;
    private String prescription;
    private Integer doctorId;
}