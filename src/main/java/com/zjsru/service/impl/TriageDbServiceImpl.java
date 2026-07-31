package com.zjsru.service.impl;

import com.zjsru.entity.AiConfig;
import com.zjsru.entity.TriageRule;
import com.zjsru.mapper.TriageMapper;
import com.zjsru.service.TriageDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TriageDbServiceImpl implements TriageDbService {

    @Autowired
    private TriageMapper triageMapper;

    @Override
    public List<TriageRule> findAllRules() {
        return triageMapper.selectAllRules();
    }

    @Override
    public void addRule(TriageRule rule) {
        triageMapper.insertRule(rule);
    }

    @Override
    public void updateRule(TriageRule rule) {
        triageMapper.updateRule(rule);
    }

    @Override
    public void deleteRule(Integer id) {
        triageMapper.deleteRuleById(id);
    }

    @Override
    public String getSystemPrompt() {
        AiConfig config = triageMapper.selectConfigByKey("system_prompt");
        return config != null ? config.getConfigValue() : "";
    }

    @Override
    public void updateSystemPrompt(String prompt) {
        int rows = triageMapper.updateConfig("system_prompt", prompt);
        if (rows == 0) {
            triageMapper.insertConfig("system_prompt", prompt);
        }
    }
}