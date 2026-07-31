package com.zjsru.mapper;

import com.zjsru.entity.AiConfig;
import com.zjsru.entity.TriageRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TriageMapper {
    // 取所有规则
    List<TriageRule> selectAllRules();

    // 添加规则
    int insertRule(TriageRule rule);

    // 修改规则
    int updateRule(TriageRule rule);

    // 删除规则
    int deleteRuleById(Integer id);

    // 取配置
    AiConfig selectConfigByKey(String key);

    // 修改配置
    int updateConfig(@Param("key") String key, @Param("value") String value);

    // 添加配置
    int insertConfig(@Param("key") String key, @Param("value") String value);
}