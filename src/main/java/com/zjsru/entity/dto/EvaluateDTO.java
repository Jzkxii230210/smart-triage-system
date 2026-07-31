package com.zjsru.entity.dto;
import lombok.Data;

@Data
public class EvaluateDTO {
    private Integer appointmentId;
    private Double rate;
    private String comment;
}