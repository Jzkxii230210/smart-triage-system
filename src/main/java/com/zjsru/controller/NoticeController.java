package com.zjsru.controller;

import com.zjsru.entity.Result;
import com.zjsru.entity.vo.NoticeVO;
import com.zjsru.service.impl.NoticeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeServiceImpl noticeService;

    // 获取公告列表
    @GetMapping("/list")
    public Result<List<NoticeVO>> getNoticeList() {
        return Result.success(noticeService.getAllNotices());
    }

    // 获取公告详情
    @GetMapping("/detail/{id}")
    public Result<NoticeVO> getNoticeDetail(@PathVariable("id") Integer id) {
        return Result.success(noticeService.getNoticeById(id));
    }
}