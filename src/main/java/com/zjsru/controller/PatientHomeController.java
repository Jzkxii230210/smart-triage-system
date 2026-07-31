package com.zjsru.controller;

import com.zjsru.entity.Result;
import com.zjsru.mapper.DashboardMapper;
import com.zjsru.entity.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patient")
public class PatientHomeController {

    @Autowired
    private DashboardMapper dashboardMapper;

    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboardData() {
        DashboardVO vo = new DashboardVO();
        vo.setDepartments(dashboardMapper.selectDepts());
        vo.setNotices(dashboardMapper.selectNotices());
        vo.setRecommendDoctors(dashboardMapper.selectRecommendDoctors());
        return Result.success(vo);
    }
}