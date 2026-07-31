package com.zjsru.controller;

import com.zjsru.entity.Result;
import com.zjsru.entity.dto.TriageDTO;
import com.zjsru.entity.vo.TriageResultVO;
import com.zjsru.service.impl.TriageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient/triage")
public class TriageController {

    @Autowired
    private TriageServiceImpl triageService;

    @PostMapping("/analyze")
    public Result<TriageResultVO> analyze(@RequestBody TriageDTO dto) {
        return Result.success(triageService.analyze(dto));
    }
}