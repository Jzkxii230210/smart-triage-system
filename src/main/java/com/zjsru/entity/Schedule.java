package com.zjsru.entity;
import lombok.Data;
import java.util.Date;

@Data
public class Schedule {
    private Integer id;
    private Integer doctorId;
    private Date workDate;
    private String slot;
    private Integer maxNum;
}