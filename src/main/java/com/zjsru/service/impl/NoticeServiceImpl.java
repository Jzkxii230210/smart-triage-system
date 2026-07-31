package com.zjsru.service.impl;

import com.zjsru.mapper.NoticeMapper;
import com.zjsru.entity.vo.NoticeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NoticeServiceImpl {

    @Autowired
    private NoticeMapper noticeMapper;

    public List<NoticeVO> getAllNotices() {
        return noticeMapper.selectAllNotices();
    }

    public NoticeVO getNoticeById(Integer id) {
        return noticeMapper.selectNoticeById(id);
    }
}