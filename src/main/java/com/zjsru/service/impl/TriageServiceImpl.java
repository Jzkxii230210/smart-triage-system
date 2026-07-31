    package com.zjsru.service.impl;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.zjsru.entity.AiConfig;
    import com.zjsru.entity.Doctor;
    import com.zjsru.entity.dto.TriageDTO;
    import com.zjsru.entity.vo.TriageResultVO;
    import com.zjsru.mapper.DoctorMapper;
    import com.zjsru.mapper.TriageMapper;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.ai.chat.client.ChatClient;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.util.StringUtils;

    import java.util.List;
    import java.util.stream.Collectors;

    @Slf4j
    @Service
    public class TriageServiceImpl {

        @Autowired
        private DoctorMapper doctorMapper;

        @Autowired
        private TriageMapper triageMapper;

        @Autowired
        private ObjectMapper objectMapper;

        private final ChatClient chatClient;

        public TriageServiceImpl(ChatClient.Builder builder) {
            this.chatClient = builder.build();
        }

        public TriageResultVO analyze(TriageDTO dto) {
            List<String> validDepts = doctorMapper.selectAllDeptNames();
            String deptListStr = String.join(", ", validDepts);

            List<Doctor> allDoctors = doctorMapper.getAllDoctors();
            StringBuilder doctorsInfo = new StringBuilder();
            for (Doctor doc : allDoctors) {
                doctorsInfo.append("医生姓名: ").append(doc.getRealName())
                        .append(", 科室: ").append(doc.getDeptName())
                        .append(", 专长: ").append(doc.getExpertise())
                        .append(", 评分: ").append(doc.getRating())
                        .append("; ");
            }

            String promptTemplate = getPromptTemplateFromDb();

            String promptText;
            try {
                promptText = String.format(promptTemplate,
                        dto.getDescription(),
                        dto.getDuration(),
                        deptListStr,
                        doctorsInfo);
            } catch (Exception e) {
                System.err.println("Prompt格式化失败，使用默认模板: " + e.getMessage());
                promptText = String.format(getDefaultPrompt(),
                        dto.getDescription(),
                        dto.getDuration(),
                        deptListStr,
                        doctorsInfo);
            }

            try {
                // 调用 AI
                String aiResponse = chatClient.prompt().user(promptText).call().content();

                if (aiResponse != null && aiResponse.contains("```json")) {
                    aiResponse = aiResponse.substring(aiResponse.indexOf("```json") + 7);
                    if (aiResponse.contains("```")) {
                        aiResponse = aiResponse.substring(0, aiResponse.indexOf("```"));
                    }
                }
                if (aiResponse != null) {
                    aiResponse = aiResponse.trim();
                }

                // 解析 AI 结果
                TriageResultVO result = objectMapper.readValue(aiResponse, TriageResultVO.class);
                List<Integer> recommendedDoctorIds = result.getRecommendedDoctorIds();

                List<Doctor> matchedDoctors;
                if (recommendedDoctorIds != null && !recommendedDoctorIds.isEmpty()) {
                    matchedDoctors = doctorMapper.getDoctorsByIds(recommendedDoctorIds);
                } else {
                    matchedDoctors = doctorMapper.selectSmartRecommendation(
                            result.getDeptName(),
                            result.getKeySymptom()
                    );
                }

                // 转换数据
                List<TriageResultVO.DoctorVO> voList = matchedDoctors.stream()
                        .limit(4)
                        .map(doc -> {
                            TriageResultVO.DoctorVO vo = new TriageResultVO.DoctorVO();
                            vo.setId(doc.getId());
                            vo.setName(doc.getRealName());
                            vo.setTitle(doc.getTitle());
                            vo.setExpertise(doc.getExpertise());
                            vo.setRating(doc.getRating());
                            return vo;
                        })
                        .collect(Collectors.toList());

                result.setDoctors(voList);
                return result;

            } catch (Exception e) {
                log.error("AI 分析过程中发生异常", e);
                TriageResultVO error = new TriageResultVO();
                error.setDeptName("导诊台");
                error.setDeptReason("AI 服务暂时繁忙，或解析结果失败，请稍后重试。");
                return error;
            }
        }

        private String getPromptTemplateFromDb() {
            try {
                AiConfig config = triageMapper.selectConfigByKey("system_prompt");
                if (config != null && StringUtils.hasText(config.getConfigValue())) {
                    return config.getConfigValue();
                }
            } catch (Exception e) {
                System.err.println("读取数据库Prompt失败，将使用默认配置");
            }
            return getDefaultPrompt();
        }

        /**
         * 默认 Prompt
         */
        private String getDefaultPrompt() {
            return """
        你是一名三甲医院导诊专家。根据患者描述进行分诊，并推荐最适合的医生。
        
        【患者描述】"%s"
        【持续时间】"%s"
        【可选科室库】%s
        【医生信息】%s
        
        【任务】
        1. 从【可选科室库】中精准匹配一个科室 (deptName)。
        2. 提取1个最核心的"症状关键词" (keySymptom)，不要太长。
        3. 根据患者症状和【医生信息】，推荐最合适的医生ID列表 (recommendedDoctorIds)。
        4. 如果没有合适科室，默认返回"内科"。
        
        【返回JSON格式】
        {
            "deptName": "选中的科室名",
            "keySymptom": "关键词",
            "deptReason": "分析理由",
            "advices": ["建议1", "建议2"],
            "recommendedDoctorIds": [1, 2, 3]
        }
        """;
        }
    }
