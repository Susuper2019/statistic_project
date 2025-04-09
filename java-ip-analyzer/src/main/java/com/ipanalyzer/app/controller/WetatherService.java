package com.ipanalyzer.app.controller;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class WetatherService {
    @Tool(description = "根据城市名称获取天气信息")
    public String getWertherCity(@ToolParam(description = "城市名称,省份,如河南省、北京市") String sheng
            , @ToolParam(description = "市区，如昌平区、安阳市") String shiqu) {
        System.out.println("工具调用：查询" + sheng + shiqu + "的天气信息");
        return "晴朗";
    }

    @Tool(description = "发送邮件给某人")
    public String sendMail(@ToolParam(description = "邮箱地址") String mailAdress, @ToolParam(description = "邮件内容") String content) {
        System.out.println("工具调用：发送邮件给" + mailAdress + "，内容为" + content);
        return "邮件已发送";
    }


    @Bean
    public ToolCallbackProvider weatherTools(WetatherService wetatherService) {
        return MethodToolCallbackProvider.builder().toolObjects(wetatherService).build();
    }
}
