package com.zjsru.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Doctor {
    private Integer id;
    private Integer userId;
    private Integer deptId;
    private String realName;
    private String title;
    private BigDecimal fee;
    private Integer stock;
    private String expertise;
    private Integer rating;
    private String deptName;
    private String deptIcon;
    private String avatar;
}