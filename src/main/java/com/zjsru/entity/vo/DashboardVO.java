package com.zjsru.entity.vo;
import lombok.Data;
import java.util.List;

@Data
public class DashboardVO {
    private List<DeptVO> departments;
    private List<NoticeVO> notices;
    private List<DoctorVO> recommendDoctors;

    @Data
    public static class DeptVO {
        private String name;
        private String icon;
    }

    @Data
    public static class NoticeVO {
        private Integer id;
        private String title;
        private String type;
        private String tag;
        private String content;
        private String createTime;
    }

    @Data
    public static class DoctorVO {
        private Integer id;
        private String name;
        private String dept_id;
        private String fee;
        private String title;
        private String expertise;
        private String stock;
    }
}