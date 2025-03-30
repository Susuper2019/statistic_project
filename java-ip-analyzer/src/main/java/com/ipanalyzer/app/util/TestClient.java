package com.ipanalyzer.app.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 简单的HTTP客户端测试工具，用于测试API
 */
public class TestClient {
    
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    public static void main(String[] args) {
        try {
            // 测试健康检查
            testHealthCheck();
            
            // 测试数据分析
            testAnalyze("1h");
            
            // 也可以测试其他间隔
            // testAnalyze("30m");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testHealthCheck() throws IOException, InterruptedException {
        System.out.println("================ 测试健康检查接口 ================");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/health"))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("状态码: " + response.statusCode());
        System.out.println("响应体: " + response.body());
        System.out.println();
    }
    
    private static void testAnalyze(String interval) throws IOException, InterruptedException {
        System.out.println("================ 测试数据分析接口 (" + interval + ") ================");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/analyze?interval=" + interval))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("状态码: " + response.statusCode());
        
        // 打印截断的响应（太长的话会影响可读性）
        String body = response.body();
        int maxLength = 2000;
        
        if (body.length() > maxLength) {
            System.out.println("响应体 (截断): " + body.substring(0, maxLength) + "...");
            System.out.println("响应总长度: " + body.length() + " 字符");
        } else {
            System.out.println("响应体: " + body);
        }
        
        System.out.println();
    }
} 