package com.zjsru.service.impl;

import com.zjsru.entity.vo.StatsVO;
import com.zjsru.mapper.StatsMapper;
import com.zjsru.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private StatsMapper statsMapper;

    @Override
    public StatsVO getDashboardData() {
        StatsVO vo = new StatsVO();

        // 1. 查询科室占比 (Pie)
        vo.setDeptStats(statsMapper.countByDept());

        // 2. 查询近7日趋势 (Line)
        vo.setTrendStats(statsMapper.countByDateTrend());

        // 3. 查询热门医生 (Bar)
        vo.setDoctorStats(statsMapper.countByDoctorTop());

        return vo;
    }
}