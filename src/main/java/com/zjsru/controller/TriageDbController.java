package com.zjsru.controller;

import com.zjsru.entity.Result;
import com.zjsru.entity.TriageRule;
import com.zjsru.service.TriageDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/triage")
public class TriageDbController {

    @Autowired
    private TriageDbService triageService;


    @GetMapping("/rules")
    public Result<List<TriageRule>> getRules() {
        return Result.success(triageService.findAllRules());
    }

    @PostMapping("/rule/save")
    public Result<?> saveRule(@RequestBody TriageRule rule) {
        if (rule.getId() == null) {
            triageService.addRule(rule);
        } else {
            triageService.updateRule(rule);
        }
        return Result.success("保存成功");
    }

    @DeleteMapping("/rule/{id}")
    public Result<?> deleteRule(@PathVariable Integer id) {
        triageService.deleteRule(id);
        return Result.success("删除成功");
    }


    @GetMapping("/prompt")
    public Result<String> getPrompt() {
        String prompt = triageService.getSystemPrompt();
        return Result.success(prompt);
    }

    @PostMapping("/prompt/save")
    public Result<?> savePrompt(@RequestBody Map<String, String> params) {
        String prompt = params.get("prompt");
        triageService.updateSystemPrompt(prompt);
        return Result.success("配置已更新");
    }
}