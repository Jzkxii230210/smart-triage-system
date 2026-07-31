package com.zjsru.service;

import com.zjsru.entity.Consultation;
import java.util.List;
import java.util.Map;

public interface ConsultationService {
    // 获取医生会诊列表
    List<Consultation> getDoctorConsultations(Integer doctorId);

    // 获取患者会诊列表
    List<Map<String, Object>> getChatHistory(Integer consultationId);

    // 发送消息
    void sendMessage(Integer consultationId, String content, String senderRole);

    // 结束会诊
    void endConsultation(Integer consultationId);
}