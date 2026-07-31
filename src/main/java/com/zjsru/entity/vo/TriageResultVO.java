package com.zjsru.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TriageResultVO {
    private String deptName;
    private String deptReason;
    private String keySymptom;
    private List<String> advices;
    private List<DoctorVO> doctors;
    private List<Integer> recommendedDoctorIds;

    @Data
    public static class DoctorVO {
        private Integer id;
        private String name;
        private String title;
        private String expertise;
        private Integer rating;
        private String avatar;
    }

    public static TriageResultVO error(String message) {
        TriageResultVO result = new TriageResultVO();
        result.setDeptName("分诊失败");
        result.setDeptReason(message);
        result.setAdvices(new ArrayList<>());
        result.setDoctors(new ArrayList<>());
        return result;
    }
}