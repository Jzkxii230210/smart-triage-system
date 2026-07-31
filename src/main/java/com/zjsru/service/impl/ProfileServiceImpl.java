    package com.zjsru.service.impl;

    import com.zjsru.entity.AppointmentStatus;
    import com.zjsru.entity.ConsultationStatus;
    import com.zjsru.mapper.ProfileMapper;
    import com.zjsru.entity.dto.EvaluateDTO;
    import com.zjsru.entity.dto.UserUpdateDTO;
    import com.zjsru.entity.vo.AppointmentVO;
    import com.zjsru.entity.vo.ProfileVO;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import java.util.List;

    @Service
    public class ProfileServiceImpl {

        @Autowired
        private ProfileMapper profileMapper;

        public ProfileVO getUserInfo(Integer userId) {
            return profileMapper.selectUserInfo(userId);
        }

        public void updateInfo(Integer userId, UserUpdateDTO dto) {
            // 更新患者信息
            profileMapper.updatePatientInfo(userId, dto);

            // 如果提供了密码，则更新用户表
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                profileMapper.updateUserPassword(userId, dto);
            }
        }

        public List<AppointmentVO> getMyRecords(Integer userId) {
            return profileMapper.selectAppointments(userId);
        }

        public void cancelBooking(Integer bookingId) {
            profileMapper.updateBookingStatus(bookingId, AppointmentStatus.CANCELLED);
        }

        @Transactional
        public void evaluate(EvaluateDTO dto) {
            if (dto == null) {
                throw new IllegalArgumentException("评价数据不能为空");
            }

            if (dto.getAppointmentId() != null) {
                System.out.println("传入的预约ID: " + dto.getAppointmentId());
            } else {
                System.out.println("传入的预约ID为 null");
            }

            if (dto.getAppointmentId() == null) {
                throw new IllegalArgumentException("预约ID不能为空");
            }

            try {
                profileMapper.insertEvaluation(dto);
                profileMapper.updateBookingStatus(dto.getAppointmentId(),AppointmentStatus.COMPLETED);
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                throw new RuntimeException("评价提交失败: " + errorMessage, e);
            }
        }
    }
