package com.zjsru.service.impl;

import com.zjsru.entity.Schedule;
import com.zjsru.entity.dto.DoctorDTO;
import com.zjsru.mapper.DoctorMapper;
import com.zjsru.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public List<DoctorDTO> findAllDoctors() {
        return doctorMapper.selectDoctorList();
    }

    @Override
    public void addSchedule(Schedule schedule) {
        doctorMapper.insertSchedule(schedule);
    }
    @Override
    public void removeSchedule(Integer scheduleId) {
        doctorMapper.deleteScheduleById(scheduleId);
    }

}