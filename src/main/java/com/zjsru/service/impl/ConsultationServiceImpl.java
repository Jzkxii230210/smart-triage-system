package com.zjsru.service.impl;

import com.zjsru.entity.Consultation;
import com.zjsru.entity.ConsultationMessage;
import com.zjsru.entity.ConsultationStatus;
import com.zjsru.mapper.ConsultationMapper;
import com.zjsru.mapper.ConsultationMessageMapper;
import com.zjsru.service.ConsultationService;
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
public class ConsultationServiceImpl implements ConsultationService {

    @Autowired
    private ConsultationMapper consultationMapper;

    @Autowired
    private ConsultationMessageMapper messageMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<Consultation> getDoctorConsultations(Integer doctorId) {
        return consultationMapper.selectListByDoctorId(doctorId);
    }

    @Override
    public List<Map<String, Object>> getChatHistory(Integer consultationId) {
        List<Map<String, Object>> result = new ArrayList<>();

        Consultation main = consultationMapper.selectById(consultationId);
        if (main != null) {
            Map<String, Object> firstMsg = createMessageMap(0, "patient", main.getContent(), main.getCreateTime());
            result.add(firstMsg);
        }

        List<ConsultationMessage> messages = messageMapper.selectByConsultationId(consultationId);

        for (ConsultationMessage msg : messages) {
            Map<String, Object> item = createMessageMap(
                    msg.getId(),
                    msg.getSenderRole(),
                    msg.getContent(),
                    msg.getCreateTime()
            );
            result.add(item);
        }
        return result;
    }

    private Map<String, Object> createMessageMap(Integer id, String sender, String content, LocalDateTime time) {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("id", id);
        messageMap.put("sender", sender);
        messageMap.put("content", content);
        messageMap.put("time", DATE_TIME_FORMATTER.format(time));
        return messageMap;
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

        if ("doctor".equals(senderRole)) {
            consultationMapper.updateStatus(consultationId, 1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endConsultation(Integer consultationId) {
        consultationMapper.updateStatus(consultationId, ConsultationStatus.COMPLETED);
        Integer appointmentId = consultationMapper.selectById(consultationId).getAppointmentId();
        consultationMapper.updateConsultationStatus(Long.valueOf(appointmentId), ConsultationStatus.COMPLETED);

    }
}
