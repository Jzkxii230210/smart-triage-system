// MedicalRecordMapper.java
package com.zjsru.mapper;

import com.zjsru.entity.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MedicalRecordMapper {
    // 根据患者ID查询病历记录
    List<MedicalRecord> selectByPatientId(@Param("patientId") Long patientId);

    // 插入病历记录
    int insert(MedicalRecord record);

    // 根据ID更新病历记录
    int updateById(MedicalRecord record);

    // 根据ID删除病历记录
    int deleteById(@Param("id") Long id);
}
