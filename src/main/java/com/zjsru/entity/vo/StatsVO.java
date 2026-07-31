package com.zjsru.entity.vo;

import lombok.Data;
import java.util.List;

@Data
public class StatsVO {
    // 饼图数据 (name, value)
    private List<ChartData> deptStats;
    // 折线图数据 (date, count)
    private List<ChartData> trendStats;
    // 柱状图数据 (doctorName, count)
    private List<ChartData> doctorStats;

    @Data
    public static class ChartData {
        private String name;
        private Integer value;
    }
}