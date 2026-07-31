package com.zjsru.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConsultationMessage {
    private Integer id;
    private Integer consultationId;
    private String senderRole;
    private String content;
    private LocalDateTime createTime;
}