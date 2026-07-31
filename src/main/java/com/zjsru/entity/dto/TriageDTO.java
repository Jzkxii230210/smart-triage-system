package com.zjsru.entity.dto;

import lombok.Data;

@Data
public class TriageDTO {
    private Integer userId;
    private String description;
    private String duration;
    private String allergy;
}