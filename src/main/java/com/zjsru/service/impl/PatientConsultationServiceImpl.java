package com.zjsru.service.impl;

import com.zjsru.entity.Consultation;
import com.zjsru.entity.ConsultationMessage;
import com.zjsru.entity.ConsultationStatus;
import com.zjsru.mapper.PatientConsultationMapper;
import com.zjsru.mapper.ConsultationMessageMapper;
import com.zjsru.service.PatientConsultationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PatientConsultationServiceImpl implements PatientConsultationService {

    @Autowired
    private PatientConsultationMapper patientMapper;

    @Autowired
    private ConsultationMessageMapper messageMapper;

    @Override
    public List<Consultation> getPatientConsultations(Integer patientId) {
        return patientMapper.selectListByPatientId(patientId);
    }


    @Override
    public List<Map<String, Object>> getDoctorList() {
        return patientMapper.selectAllDoctors();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createConsultation(Integer patientId, String patientName, Integer doctorId, String content) {
        Consultation consultation = new Consultation();
        consultation.setPatientId(patientId);
        consultation.setPatientName(patientName);
        consultation.setDoctorId(doctorId);
        consultation.setContent(content);
        consultation.setStatus(ConsultationStatus.PENDING);
        consultation.setCreateTime(LocalDateTime.now());
        patientMapper.insert(consultation);
    }

    @Transactional(rollbackFor = Exception.class)
    public void endConsultation(Integer consultationId) {
        log.info("开始结束会诊，consultationId: {}", consultationId);

        // 1. 获取咨询记录
        Consultation consultation = patientMapper.selectById(consultationId);
        log.debug("查询到的咨询记录: {}", consultation);

        if (consultation == null) {
            log.error("咨询记录不存在，ID: {}", consultationId);
            throw new RuntimeException("咨询记录不存在，ID: " + consultationId);
        }

        // 2. 更新咨询状态
        int updatedRows = patientMapper.updateStatus(consultationId, ConsultationStatus.COMPLETED);
        log.info("更新咨询状态，consultationId: {}, affectedRows: {}", consultationId, updatedRows);

        // 3. 获取关联的预约ID并更新状态
        Integer appointmentId = consultation.getAppointmentId();
        log.debug("关联的预约ID: {}", appointmentId);

        if (appointmentId != null) {
            int appointmentUpdatedRows = patientMapper.updateAppointmentStatus(appointmentId, ConsultationStatus.COMPLETED);
            log.info("更新预约状态，appointmentId: {}, affectedRows: {}", appointmentId, appointmentUpdatedRows);
        }

        log.info("结束会诊成功，consultationId: {}", consultationId);
    }

    @Override
    public List<Map<String, Object>> getChatHistory(Integer consultationId) {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Consultation main = patientMapper.selectById(consultationId);
        if (main != null) {
            Map<String, Object> firstMsg = new HashMap<>();
            firstMsg.put("id", 0);
            firstMsg.put("sender", "patient");
            firstMsg.put("content", main.getContent());
            firstMsg.put("time", dtf.format(main.getCreateTime()));
            result.add(firstMsg);
        }

        List<ConsultationMessage> messages = messageMapper.selectByConsultationId(consultationId);

        for (ConsultationMessage msg : messages) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", msg.getId());
            item.put("sender", msg.getSenderRole());
            item.put("content", msg.getContent());
            item.put("time", dtf.format(msg.getCreateTime()));
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Integer consultationId, String content, String senderRole) {
        ConsultationMessage msg = new ConsultationMessage();
        msg.setConsultationId(consultationId);
        msg.setSenderRole(senderRole);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());

        messageMapper.insert(msg);

        if ("patient".equals(senderRole)) {
            patientMapper.updateStatus(consultationId, ConsultationStatus.ACTIVE);
        }
    }
}
