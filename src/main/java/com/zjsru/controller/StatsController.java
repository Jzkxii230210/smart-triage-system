package com.zjsru.controller;

import com.zjsru.entity.Result;
import com.zjsru.entity.vo.StatsVO;
import com.zjsru.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/dashboard")
    public Result<StatsVO> getDashboardStats() {
        StatsVO vo = statsService.getDashboardData();
        return Result.success(vo);
    }
}