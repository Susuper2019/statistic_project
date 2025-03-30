package com.ipanalyzer.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 交互分析结果，包含交互记录列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractionAnalysisResult {
    // 分析间隔
    private String interval;
    
    // 总交互数
    private int totalInteractions;
    
    // 交互记录列表
    private List<InteractionRecord> interactions;
    
    // 元数据信息
    private Metadata metadata;
    
    /**
     * 元数据，包含额外的分析信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        // 时间范围
        private String timeRange;
        
        // 独立主体(源IP)数量
        private int uniqueSubjects;
        
        // 独立客体(目的IP)数量
        private int uniqueObjects;
        
        // 分析描述
        private String description;
    }
} 