import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.HashMap;

public class McpClientDemo {
  public static void main(String[] args) {
    OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
    openAiChatOptions.setModel("gpt-4-turbo-preview");
    OpenAiChatModel openAiChatModel = OpenAiChatModel.builder().openAiApi(
            OpenAiApi.builder().baseUrl("http://localhost:3000").
                apiKey("sk-hTmGLyzDvnk8ouwBB7JFNJmzLibhIXwu6AMTX6UdxtEA3ddJ").build())
        .defaultOptions(openAiChatOptions).build();
    System.out.println(openAiChatModel.call("hello"));


//        OpenAiChatModel chatModel = new OpenAiChatModel(new OpenAiApi("http://localhost:3000", new ApiKey() {
//            @Override
//            public String getValue() {
//                return "";
//            }
//        }, ));

//        sk-hTmGLyzDvnk8ouwBB7JFNJmzLibhIXwu6AMTX6UdxtEA3ddJ

    McpSyncClient client = McpClient.sync(new HttpClientSseClientTransport("http://127.0.0.1:22015")).build();
    McpSchema.InitializeResult initialize = client.initialize();
    McpSchema.ListToolsResult listToolsResult = client.listTools();
    System.out.println(listToolsResult.tools());

    for (McpSchema.Tool tool : listToolsResult.tools()) {
      McpSchema.CallToolResult callToolResult = client.callTool(new McpSchema.CallToolRequest(tool.name(), new HashMap<>() {{
        put("city", "北京");
      }}));
      System.out.println(callToolResult.content());
    }
  }
}
