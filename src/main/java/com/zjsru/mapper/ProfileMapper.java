package com.zjsru.mapper;

import com.zjsru.entity.dto.EvaluateDTO;
import com.zjsru.entity.dto.UserUpdateDTO;
import com.zjsru.entity.vo.AppointmentVO;
import com.zjsru.entity.vo.ProfileVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProfileMapper {
    // 1. 获取用户信息
    ProfileVO selectUserInfo(@Param("userId") Integer userId);

    // 2. 更新患者信息
    int updatePatientInfo(@Param("userId") Integer userId, @Param("dto") UserUpdateDTO dto);

    // 3. 更新用户密码
    int updateUserPassword(@Param("userId") Integer userId, @Param("dto") UserUpdateDTO dto);

    // 4. 获取预约记录 (联表查询：医生、科室、评价表)
    List<AppointmentVO> selectAppointments(@Param("userId") Integer userId);

    // 5. 取消预约 (修改状态)
    int updateBookingStatus(@Param("bookingId") Integer bookingId, @Param("status") Integer status);

    // 6. 插入评价
    int insertEvaluation(EvaluateDTO dto);
}