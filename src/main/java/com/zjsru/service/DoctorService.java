package com.zjsru.service;
import com.zjsru.entity.Schedule;
import com.zjsru.entity.dto.DoctorDTO;

import java.util.List;

public interface DoctorService {
    // 查询所有医生
    List<DoctorDTO> findAllDoctors();

    // 添加排班
    void addSchedule(Schedule schedule);

    // 删除排班
    void removeSchedule(Integer scheduleId);
}