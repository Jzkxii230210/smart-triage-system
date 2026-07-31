package com.zjsru.service.impl;

import com.zjsru.entity.MedicalRecord;
import com.zjsru.mapper.MedicalRecordMapper;
import com.zjsru.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Override
    public void updateMedicalRecord(MedicalRecord record) {
        medicalRecordMapper.updateById(record);
    }

}
