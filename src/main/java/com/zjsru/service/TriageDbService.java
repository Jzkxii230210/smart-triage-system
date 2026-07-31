package com.zjsru.service;

import com.zjsru.entity.TriageRule;
import java.util.List;

public interface TriageDbService {
    // 获取所有规则
    List<TriageRule> findAllRules();

    // 添加规则
    void addRule(TriageRule rule);

    // 修改规则
    void updateRule(TriageRule rule);

    // 删除规则
    void deleteRule(Integer id);

    // 获取系统提示
    String getSystemPrompt();

    // 修改系统提示
    void updateSystemPrompt(String prompt);
}