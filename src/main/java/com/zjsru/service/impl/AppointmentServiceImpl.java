    package com.zjsru.service.impl;

    import com.zjsru.entity.Appointment;
    import com.zjsru.entity.AppointmentStatus;
    import com.zjsru.entity.Doctor;
    import com.zjsru.entity.dto.AppointmentDTO;
    import com.zjsru.entity.vo.AppointmentVO;
    import com.zjsru.mapper.AppointmentMapper;
    import com.zjsru.mapper.DoctorMapper;
    import com.zjsru.service.AppointmentService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Map;

    @Service
    public class AppointmentServiceImpl implements AppointmentService {
        @Autowired
        private AppointmentMapper appointmentMapper;

        @Autowired
        private DoctorMapper doctorMapper;

        @Override
        public List<Map<String, Object>> getDeptList() {
            return appointmentMapper.selectAllDepts();
        }

        @Override
        public List<AppointmentVO> getDoctorList(String deptName, String date) {
            if ("0".equals(deptName)) {
                deptName = null;
            }
            if (date == null || date.isEmpty()) {
                date = LocalDate.now().toString();
            }
            return appointmentMapper.selectDoctorSchedule(deptName, date);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void submitAppointment(AppointmentDTO dto, Integer userId) {
            // 1. 校验医生是否存在 & 获取库存
            Integer stock = appointmentMapper.selectDoctorStock(dto.getDoctorId());
            if (stock == null) {
                throw new RuntimeException("该医生不存在");
            }

            Doctor doctor = doctorMapper.selectDoctorById(dto.getDoctorId());

            if (doctor == null) {
                throw new RuntimeException("医生信息不存在");
            }

            // 检查并处理 null 的 deptId
            if (doctor.getDeptId() == null) {
                // 从关联查询中获取科室信息
                doctor = doctorMapper.selectDoctorWithDeptById(dto.getDoctorId());
                if (doctor.getDeptId() == null) {
                    throw new RuntimeException("医生科室信息缺失，无法完成预约");
                }
            }

            // 3. 校验剩余号源
            int used = appointmentMapper.countAppointment(dto.getDoctorId(), dto.getAppointmentDate());
            if (used >= stock) {
                throw new RuntimeException("号源已抢光！");
            }

            // 4. 组装实体对象
            Appointment appointment = new Appointment();
            appointment.setPatientId(userId);
            appointment.setDoctorId(dto.getDoctorId());
            appointment.setDeptId(doctor.getDeptId());
            appointment.setAppointmentDate(LocalDate.parse(dto.getAppointmentDate()));
            appointment.setTimeSlot(dto.getTimeSlot());
            appointment.setSlot(dto.getSlot());
            appointment.setCreateTime(LocalDateTime.now());
            appointment.setStatus(AppointmentStatus.BOOKED);
            appointment.setFee(doctor.getFee());

            appointmentMapper.insertAppointment(appointment);
        }

    }
