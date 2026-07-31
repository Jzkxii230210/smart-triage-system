    package com.zjsru.mapper;

    import com.zjsru.entity.Appointment;
    import com.zjsru.entity.vo.AppointmentVO;
    import org.apache.ibatis.annotations.Mapper;
    import org.apache.ibatis.annotations.Param;

    import java.util.List;
    import java.util.Map;

    @Mapper
    public interface AppointmentMapper {
        // 1. 获取科室列表
        List<Map<String, Object>> selectAllDepts();

        // 2. 获取医生列表（带剩余号源计算）
        List<AppointmentVO> selectDoctorSchedule(@Param("deptName") String deptName, @Param("date") String date);

        // 3. 统计某医生某天已预约数
        int countAppointment(@Param("doctorId") Integer doctorId, @Param("date") String date);

        // 4. 查询医生库存 (为了校验)
        Integer selectDoctorStock(@Param("doctorId") Integer doctorId);

        // 5. 插入预约记录
        int insertAppointment(Appointment appointment);
    }
