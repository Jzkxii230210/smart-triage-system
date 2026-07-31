package com.zjsru.service.impl;

import com.zjsru.entity.Consultation;
import com.zjsru.entity.ConsultationStatus;
import com.zjsru.entity.MedicalRecord;
import com.zjsru.entity.dto.AppointmentStatusDTO;
import com.zjsru.entity.vo.*;
import com.zjsru.mapper.ConsultationMapper;
import com.zjsru.mapper.DoctorMapper;
import com.zjsru.mapper.MedicalRecordMapper;
import com.zjsru.mapper.PatientManagerMapper;
import com.zjsru.service.PatientManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PatientManagerServiceImpl implements PatientManagerService {

    @Autowired
    private PatientManagerMapper patientManagerMapper;

    @Autowired
    private ConsultationMapper consultationMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;
    @Override
    public PatientProfileVO getPatientProfile(Long patientId, Integer doctorId) {
        PatientProfileVO profile = patientManagerMapper.selectPatientBasicInfo(patientId);
        if (profile == null) {
            return null;
        }

        List<AppointmentVO> appointments = patientManagerMapper.selectAppointmentsByPatientId(patientId, doctorId);
        profile.setAppointments(appointments);

        List<MedicalRecordVO> medicalRecords = patientManagerMapper.selectMedicalRecordsByPatientId(patientId);
        profile.setMedicalRecords(medicalRecords);

        return profile;
    }

    @Override
    public List<PatientListVO> getDoctorRelatedPatients(Integer doctorId, String keyword) {
        return patientManagerMapper.selectDoctorRelatedPatients(doctorId, keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAppointmentStatus(AppointmentStatusDTO dto) {
        // 1. 校验参数
        if (dto.getAppointmentId() == null || dto.getStatus() == null) {
            throw new RuntimeException("参数错误");
        }

        patientManagerMapper.updateAppointmentStatus(dto);

        if (Objects.equals(dto.getStatus(), ConsultationStatus.ACTIVE)) {
            createConsultationIfNotExist(dto);
        }
        else if (dto.getStatus().equals(ConsultationStatus.COMPLETED)) {
            createMedicalRecordFromAppointment(dto);
        }
    }

    private void createMedicalRecordFromAppointment(AppointmentStatusDTO dto) {
        if(dto.getDiagnosis() == null || dto.getDiagnosis().isEmpty()) {
            throw new RuntimeException("诊断结果不能为空，无法创建病历记录");
        }

        Long patientId = patientManagerMapper.getPatientIdByAppointmentId(dto.getAppointmentId());

        if (patientId == null) {
            throw new RuntimeException("未找到关联的患者信息");
        }

        MedicalRecord record = new MedicalRecord();
        record.setAppointmentId(dto.getAppointmentId());
        record.setPatientId(patientId);
        record.setDoctorId(dto.getDoctorId().longValue());
        record.setDoctorName(doctorMapper.findNameById(dto.getDoctorId()));
        record.setDiagnosis(dto.getDiagnosis());
        record.setPrescription(dto.getPrescription());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        medicalRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePatientCascade(Long patientId) {
        patientManagerMapper.deleteMedicalRecordsByPatientId(patientId);
        patientManagerMapper.deleteAppointmentsByPatientId(patientId);
        int rows = patientManagerMapper.deletePatientById(patientId);
        if (rows == 0) throw new RuntimeException("患者不存在");
    }

    private void createConsultationIfNotExist(AppointmentStatusDTO dto) {
        Map<String, Object> info = patientManagerMapper.getAppointmentSimpleInfo(dto.getAppointmentId());
        if (info == null) return;

        Integer doctorId = null;
        if (info.get("doctorId") != null) {
            doctorId = ((Number) info.get("doctorId")).intValue();
        }

        Integer patientId = null;
        if (info.get("patientId") != null) {
            patientId = ((Number) info.get("patientId")).intValue();
        }

        Long appointmentId = dto.getAppointmentId();

        if (doctorId == null || patientId == null || appointmentId == null) {
            return;
        }

        int activeCount = consultationMapper.countActiveConsultation(doctorId, patientId, appointmentId);
        if (activeCount > 0) {
            throw new RuntimeException("存在还未结束的会诊对话，无法创建新的咨询");
        }

        String patientName = patientManagerMapper.getPatientNameById(patientId);
        String doctorName = doctorMapper.findNameById(doctorId);

        Consultation consult = new Consultation();
        consult.setAppointmentId(appointmentId.intValue());
        consult.setDoctorId(doctorId);
        consult.setPatientId(patientId);
        consult.setPatientName(patientName);
        consult.setDoctorName(doctorName);
        consult.setContent("【系统消息】医生已接诊,请详细描述您的病情.");
        consult.setStatus(ConsultationStatus.ACTIVE);
        consult.setCreateTime(LocalDateTime.now());

        consultationMapper.insertConsultation(consult);
    }


}