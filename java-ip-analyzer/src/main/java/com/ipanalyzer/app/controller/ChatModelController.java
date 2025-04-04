package com.ipanalyzer.app.controller;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
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

  @GetMapping(value = "run")
  public String run(@RequestParam(value = "prompt") String prompt) {
    McpSyncClient client = McpClient.sync(new HttpClientSseClientTransport("http://127.0.0.1:22015")).build();
    McpSchema.InitializeResult initialize = client.initialize();
    McpSchema.ListToolsResult listToolsResult = client.listTools();

    SyncMcpToolCallbackProvider syncMcpToolCallbackProvider = new SyncMcpToolCallbackProvider(List.of(client));

    ChatClient chatClient = ChatClient.builder(openAiChatModel).defaultTools(syncMcpToolCallbackProvider).build();
    String content = chatClient.prompt(prompt).call().content();
    System.out.println(content);
    return content;
  }
}
