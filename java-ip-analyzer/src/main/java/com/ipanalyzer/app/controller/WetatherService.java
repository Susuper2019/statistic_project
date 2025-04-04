package com.ipanalyzer.app.controller;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class WetatherService {
    @Tool(description = "根据城市名称获取天气信息")
    public String getWertherCity(@ToolParam(description = "城市名称,省份,如河南省、北京市") String sheng
        , @RequestParam(defaultValue = "市区，如昌平区、安阳市") String shiqu) {
      System.out.println("查询" + sheng + shiqu + "的天气信息");
      return "晴朗";
    }

    @Bean
    public ToolCallbackProvider weatherTools(WetatherService wetatherService) {
        return MethodToolCallbackProvider.builder().toolObjects(wetatherService).build();
    }
}
