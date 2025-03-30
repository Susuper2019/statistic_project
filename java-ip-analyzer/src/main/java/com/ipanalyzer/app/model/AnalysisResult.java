package com.ipanalyzer.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String interval;
    private Map<String, Map<String, Map<String, Integer>>> data;
} 