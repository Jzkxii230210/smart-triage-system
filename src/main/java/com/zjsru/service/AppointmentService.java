    package com.zjsru.service;

    import com.zjsru.entity.dto.AppointmentDTO;
    import com.zjsru.entity.vo.AppointmentVO;

    import java.util.List;
    import java.util.Map;

    public interface AppointmentService {
        // 获取科室列表
        List<Map<String, Object>> getDeptList();

        // 获取医生列表
        List<AppointmentVO> getDoctorList(String deptName, String date);

        // 提交预约
        void submitAppointment(AppointmentDTO dto, Integer userId);
    }
