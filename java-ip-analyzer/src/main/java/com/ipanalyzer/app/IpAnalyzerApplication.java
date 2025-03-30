package com.ipanalyzer.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class IpAnalyzerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(IpAnalyzerApplication.class, args);
    }
} 