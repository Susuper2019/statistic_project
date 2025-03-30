package com.ipanalyzer.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 表示一条主体与客体之间的交互记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractionRecord {
    // 时间段开始时间
    private LocalDateTime startTime;
    
    // 时间段结束时间
    private LocalDateTime endTime;
    
    // 交互次数
    private int count;
    
    // 主体 (源IP)
    private String subject;
    
    // 客体 (目的IP)
    private String object;
    
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//    /**
//     * 将日期时间格式化为字符串
//     */
//    public String getFormattedStartTime() {
//        if (startTime == null) return null;
//        return startTime.format(OUTPUT_FORMATTER);
//    }
//
//    /**
//     * 将日期时间格式化为字符串
//     */
//    public String getFormattedEndTime() {
//        if (endTime == null) return null;
//        return endTime.format(OUTPUT_FORMATTER);
//    }
} 