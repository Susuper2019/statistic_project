package com.ipanalyzer.app.controller;

import com.ipanalyzer.app.model.AnalysisResult;
import com.ipanalyzer.app.model.ApiResponse;
import com.ipanalyzer.app.model.InteractionAnalysisResult;
import com.ipanalyzer.app.service.IpAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class IpAnalyzerController {

    private final IpAnalyzerService ipAnalyzerService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(ApiResponse.error("没有选择文件"));
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !(filename.endsWith(".xlsx") || filename.endsWith(".xls") || filename.endsWith(".csv"))) {
                return ResponseEntity
                        .badRequest()
                        .body(ApiResponse.error("不支持的文件类型"));
            }

            Map<String, Object> result = ipAnalyzerService.processFile(file);

            return ResponseEntity.ok(ApiResponse.success("文件上传成功并已处理", result));

        } catch (Exception e) {
            log.error("处理上传文件时出错", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("处理文件时出错: " + e.getMessage()));
        }
    }

    @GetMapping("/analyze")
    public ResponseEntity<ApiResponse<AnalysisResult>> analyzeData(
            @RequestParam(value = "interval", defaultValue = "1h") String interval) {
        try {
            AnalysisResult result = ipAnalyzerService.analyzeData(interval);
            return ResponseEntity.ok(ApiResponse.success("数据已按" + interval + "间隔汇总", result));

        } catch (FileNotFoundException e) {
            log.error("数据文件不存在", e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (NumberFormatException e) {
            log.error("无效的间隔格式", e);
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("无效的间隔格式，请使用如 '1h' 或 '30m'"));

        } catch (Exception e) {
            log.error("分析数据时出错", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("分析数据时出错: " + e.getMessage()));
        }
    }

    @GetMapping("/analyze/interactions")
    public ResponseEntity<ApiResponse<InteractionAnalysisResult>> analyzeInteractions(
            @RequestParam(value = "interval", defaultValue = "1h") String interval) {
        try {
            InteractionAnalysisResult result = ipAnalyzerService.analyzeInteractions(interval);
            return ResponseEntity.ok(ApiResponse.success("主体客体交互数据已按" + interval + "间隔汇总", result));

        } catch (FileNotFoundException e) {
            log.error("数据文件不存在", e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (NumberFormatException e) {
            log.error("无效的间隔格式", e);
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("无效的间隔格式，请使用如 '1h' 或 '30m'"));

        } catch (Exception e) {
            log.error("分析交互数据时出错", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("分析交互数据时出错: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("OK", "服务运行正常"));
    }
}