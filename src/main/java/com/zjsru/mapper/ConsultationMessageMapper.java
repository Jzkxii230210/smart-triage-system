package com.zjsru.mapper;

import com.zjsru.entity.ConsultationMessage;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ConsultationMessageMapper {
    // 查询某会话的所有消息
    List<ConsultationMessage> selectByConsultationId(Integer consultationId);

    // 插入消息
    int insert(ConsultationMessage message);
}