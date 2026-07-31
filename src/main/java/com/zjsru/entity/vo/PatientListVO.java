package com.zjsru.entity.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientListVO {
    private Long id;             // 患者ID
    private String name;         // 姓名
    private String gender;       // 性别
    private Integer age;         // 年龄
    private String phone;        // 电话
    private LocalDate lastVisit; // 最近就诊日期
}