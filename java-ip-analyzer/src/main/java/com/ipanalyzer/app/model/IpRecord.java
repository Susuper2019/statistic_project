package com.ipanalyzer.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpRecord {
    private String srcIp;
    private String dstIp;
    private LocalDateTime time;
} 