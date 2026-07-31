package com.zjsru.entity.dto;
import com.zjsru.entity.Schedule;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DoctorDTO  {
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
    private List<Schedule> schedules;
}