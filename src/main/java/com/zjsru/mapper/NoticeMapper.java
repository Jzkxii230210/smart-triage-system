package com.zjsru.mapper;

import com.zjsru.entity.vo.NoticeVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface NoticeMapper {
    // 查询所有公告（按日期倒序）
    List<NoticeVO> selectAllNotices();

    // 根据ID查询详情
    NoticeVO selectNoticeById(Integer id);
}