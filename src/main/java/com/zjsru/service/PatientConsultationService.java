package com.zjsru.service;

import com.zjsru.entity.Consultation;
import java.util.List;
import java.util.Map;

public interface PatientConsultationService {
    // 获取患者咨询列表
    List<Consultation> getPatientConsultations(Integer patientId);

    // 获取医生列表
    List<Map<String, Object>> getDoctorList();

    // 创建咨询
    void createConsultation(Integer patientId, String patientName, Integer doctorId, String content);

    // 结束咨询
    void endConsultation(Integer consultationId);

    // 获取咨询聊天记录
    List<Map<String, Object>> getChatHistory(Integer consultationId);

    // 发送消息
    void sendMessage(Integer consultationId, String content, String senderRole);
}
