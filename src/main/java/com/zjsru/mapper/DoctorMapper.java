package com.zjsru.mapper;

import com.zjsru.entity.Doctor;
import com.zjsru.entity.Schedule;
import com.zjsru.entity.dto.DoctorDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoctorMapper {
    // 科室推荐
    List<Doctor> selectSmartRecommendation(@Param("deptName") String deptName, @Param("keyword") String keyword);

    // 获取医生信息
    Doctor selectDoctorById(Integer doctorId);

    // 获取医生列表
    List<DoctorDTO> selectDoctorList();

    // 插入排班信息
    int insertSchedule(Schedule schedule);

    // 获取科室列表
    List<String> selectAllDeptNames();

    // 删除排班信息
    int deleteScheduleById(Integer id); // 新增删除方法

    // 根据用户名查询医生ID
    Integer findIdByUsername(String username);

    Doctor selectDoctorWithDeptById(Integer id);

    String findNameById(Integer doctorId);

    List<Doctor> getAllDoctors();

    List<Doctor> getAllDoctorsWithExpertise();

    List<Doctor> getDoctorsByIds(@Param("ids") List<Integer> ids);
}