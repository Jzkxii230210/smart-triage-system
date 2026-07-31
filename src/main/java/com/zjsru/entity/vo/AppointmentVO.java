package com.zjsru.entity.vo;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AppointmentVO {
    private Integer id;
    private String date;        // 预约日期
    private String realName;    // 医生姓名
    private String title;       // 职称
    private String deptName;        // 科室名称
    private BigDecimal fee;     // 挂号费
    private int status;      // 状态
    private String timeSlot;
    private String expertise;   // 擅长领域
    private Boolean isEvaluated;// 是否已评价
    private Integer rate;       // 评价等级
    private Integer rest;       // 剩余号源
    private String comment;     // 评价内容
}