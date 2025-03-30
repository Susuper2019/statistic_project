package com.ipanalyzer.app.service;

import com.ipanalyzer.app.model.AnalysisResult;
import com.ipanalyzer.app.model.InteractionAnalysisResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface IpAnalyzerService {
    
    /**
     * 上传并处理文件
     * 
     * @param file 上传的文件
     * @return 处理结果
     * @throws IOException 文件处理异常
     */
    Map<String, Object> processFile(MultipartFile file) throws IOException;
    
    /**
     * 按时间间隔分析数据
     * 
     * @param interval 时间间隔 (如 "1h", "30m")
     * @return 分析结果
     * @throws IOException 文件处理异常
     */
    AnalysisResult analyzeData(String interval) throws IOException;
    
    /**
     * 按时间间隔分析数据，返回新格式的分析结果
     * 
     * @param interval 时间间隔 (如 "1h", "30m")
     * @return 交互分析结果
     * @throws IOException 文件处理异常
     */
    InteractionAnalysisResult analyzeInteractions(String interval) throws IOException;
} 