package com.zjsru.mapper;

import com.zjsru.entity.vo.StatsVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StatsMapper {
    // 统计各科室挂号数量
    List<StatsVO.ChartData> countByDept();

    // 统计近7天每天的挂号量
    List<StatsVO.ChartData> countByDateTrend();

    // 统计挂号量最多的前5名医生
    List<StatsVO.ChartData> countByDoctorTop();
}