package com.zjsru.controller;

import com.zjsru.entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/doctor/ai")
public class AiDiagnosisController {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    private final WebClient webClient;

    public AiDiagnosisController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * 场景一：纯文本对话 - 调用 qwen-max
     */
    @PostMapping(value = "/diagnosis/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result chatJson(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "");
        String context = body.getOrDefault("context", "");
        String fullPrompt = context + "\n医生：" + message + "\nAI助手：";

        // 1. 构建 Native API 请求体结构，包含系统提示词
        List<Map<String, String>> messages = new ArrayList<>();
        // 添加系统提示词
            messages.add(Map.of("role", "system", "content", "你是临床决策辅助系统(CDSS)，需基于医生提供的病例信息进行分析。" +
                "回答需包含：可能诊断（按可能性排序）、鉴别诊断、建议检查、处理原则。" +
                "使用Markdown格式，重点内容加粗。"));
        // 添加用户消息
        messages.add(Map.of("role", "user", "content", fullPrompt));

        Map<String, Object> input = new HashMap<>();
        input.put("messages", messages);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen-max");
        requestBody.put("input", input);

        // 可选参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("result_format", "text"); // 简化返回格式
        requestBody.put("parameters", parameters);

        try {
            Map response = webClient.post()
                    .uri("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 3. 解析响应 (Native API 的 output.text 字段)
            if (response != null && response.containsKey("output")) {
                Map output = (Map) response.get("output");
                // 检查是否发生错误
                if (output.get("text") != null) {
                    return Result.success((String) output.get("text"));
                }
            }
            return Result.error("AI 响应格式异常: " + response);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("API调用失败: " + e.getMessage());
        }
    }

    /**
     * 场景二：带图片的对话 - 调用 qwen-vl-max
     */
    @PostMapping(value = "/diagnosis/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result chatMultipart(
            @RequestParam(value = "message", defaultValue = "请根据图片分析病情") String message,
            @RequestParam(value = "context", defaultValue = "") String context,
            @RequestParam(value = "imageFile") MultipartFile imageFile
    ) throws IOException {

        // 1. 图片转 Base64 (Data URI 格式)
        String mimeType = imageFile.getContentType();
        if (mimeType == null) mimeType = "image/png";
        String base64Data = Base64.getEncoder().encodeToString(imageFile.getBytes());
        String dataUri = "data:" + mimeType + ";base64," + base64Data;

        String fullPrompt = context + "\n医生：" + message + "\nAI助手：";

        // 2. 构建 VL 模型请求体，包含系统提示词
        List<Map<String, Object>> contentList = new ArrayList<>();
        contentList.add(Map.of("image", dataUri));
        contentList.add(Map.of("text", fullPrompt));

        // 构建消息列表，包含系统提示词
        List<Map<String, Object>> messages = new ArrayList<>();
        // 添加系统提示词
        messages.add(Map.of("role", "system", "content", "你是临床决策辅助系统(CDSS)，需基于医生提供的病例信息进行分析。" +
                "回答需包含：可能诊断（按可能性排序）、鉴别诊断、建议检查、处理原则。" +
                "使用Markdown格式，重点内容加粗。"));
        // 添加用户消息（包含图片和文本）
        messages.add(Map.of("role", "user", "content", contentList));

        Map<String, Object> input = new HashMap<>();
        input.put("messages", messages);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen-vl-max");
        requestBody.put("input", input);

        try {
            Map response = webClient.post()
                    .uri("https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("X-DashScope-DataInspection", "enable") // 可选：开启内容检测
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 3. 解析 VL 模型响应
            // VL 模型的返回结构通常在 output.choices[0].message.content 里面
            if (response != null && response.containsKey("output")) {
                Map output = (Map) response.get("output");
                if (output.containsKey("choices")) {
                    List choices = (List) output.get("choices");
                    if (!choices.isEmpty()) {
                        Map firstChoice = (Map) choices.get(0);
                        Map msg = (Map) firstChoice.get("message");
                        Object contentObj = msg.get("content");
                        if (contentObj instanceof List) {
                            // 标准格式：content 是一个 list，里面包含 text 对象
                            List list = (List) contentObj;
                            for (Object item : list) {
                                Map mapItem = (Map) item;
                                if (mapItem.containsKey("text")) {
                                    return Result.success((String) mapItem.get("text"));
                                }
                            }
                        }
                    }
                }
            }
            return Result.error("未能解析 AI 返回结果");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("API调用失败: " + e.getMessage());
        }
    }
}
