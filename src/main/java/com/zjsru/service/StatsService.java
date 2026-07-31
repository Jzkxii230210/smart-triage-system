package com.zjsru.service;
import com.zjsru.entity.vo.StatsVO;

public interface StatsService {
    // 获取统计数据
    StatsVO getDashboardData();
}