package com.ipanalyzer.app.controller;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author gjd
 */
@RestController
@RequestMapping("/model")
@RequiredArgsConstructor
@Slf4j
public class ChatModelController {
    private final OpenAiChatModel openAiChatModel;

    @GetMapping(value = "run",produces = "text/plain;charset=UTF-8")
    public String run(@RequestParam(value = "prompt", defaultValue = "发邮件给zs@123.com，内容是查询到的北京昌平区的天气情况") String prompt,
                      HttpServletResponse response) {
      response.setContentType("text/plain;charset=UTF-8");
      McpSyncClient client = McpClient.sync(new HttpClientSseClientTransport("http://127.0.0.1:22015")).build();
        McpSchema.InitializeResult initialize = client.initialize();
        McpSchema.ListToolsResult listToolsResult = client.listTools();

        SyncMcpToolCallbackProvider syncMcpToolCallbackProvider = new SyncMcpToolCallbackProvider(List.of(client));

        ChatClient chatClient = ChatClient.builder(openAiChatModel).defaultTools(syncMcpToolCallbackProvider).build();
        String content = chatClient.prompt(prompt).call().content();
        System.out.println("大模型返回值：" + content);
        return content;
    }
}
