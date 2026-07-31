    package com.zjsru.entity;

    import lombok.Data;
    import java.time.LocalDateTime;

    @Data
    public class Consultation {
        private Integer id;
        private Integer appointmentId;
        private Integer patientId;
        private Integer doctorId;
        private String patientName;
        private String doctorName;
        private String content;
        private Integer status;
        private LocalDateTime createTime;
    }