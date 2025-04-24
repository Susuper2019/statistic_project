package com.ipanalyzer.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class IpAnalyzerApplication {
    static {
        System.setProperty("30", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(IpAnalyzerApplication.class, args);
    }
} 