package com.ipanalyzer.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
@EnableWebMvc
class McpConfig {
  @Bean
  WebMvcSseServerTransportProvider webMvcSseServerTransport(ObjectMapper mapper) {
    return new WebMvcSseServerTransportProvider(mapper, "/mcp/message2223");
  }

  @Bean
  RouterFunction<ServerResponse> mcpRouterFunction(WebMvcSseServerTransportProvider transport) {

    return transport.getRouterFunction();
  }

  public static void main(String[] args) {
    WebMvcSseServerTransportProvider transportProvider = new WebMvcSseServerTransportProvider(new ObjectMapper(), "/mcp/message2223");
    // 创建具有自定义配置的服务器
    McpSyncServer syncServer = McpServer.sync(transportProvider)
        .serverInfo("my-server", "1.0.0")
        .capabilities(McpSchema.ServerCapabilities.builder()
            .resources(true, true)     // 启用资源支持
            .tools(true)         // 启用工具支持
            .prompts(true)       // 启用提示支持
            .logging()           // 启用日志支持
            .build())
        .build();

// 注册工具、资源和提示
//        syncServer.addTool(syncToolRegistration);
//        syncServer.addResource(syncResourceRegistration);
//        syncServer.addPrompt(syncPromptRegistration);

// 发送日志通知
    syncServer.loggingNotification(McpSchema.LoggingMessageNotification.builder()
        .level(McpSchema.LoggingLevel.INFO)
        .logger("custom-logger")
        .data("服务器已初始化")
        .build());

// 完成后关闭服务器
//        syncServer.close();
  }
}