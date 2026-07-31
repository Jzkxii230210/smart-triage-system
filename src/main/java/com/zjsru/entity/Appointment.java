    package com.zjsru.entity;

    import lombok.Data;

    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.time.LocalDateTime;

    @Data
    public class Appointment {
        private Integer id;
        private Integer patientId;
        private Integer doctorId;
        private String realName;
        private Integer deptId;
        private LocalDate appointmentDate;
        private String timeSlot;
        private String slot;
        private LocalDateTime createTime;
        private Integer status;
        private BigDecimal fee;
        private String diagnosis;
        private String prescription;
    }
