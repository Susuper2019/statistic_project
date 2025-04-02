import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallback;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.util.ToolUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class McpClientDemo {
  public static void main(String[] args) {
//    OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
//    openAiChatOptions.setModel("gpt-4-turbo-preview");
//    OpenAiChatModel openAiChatModel = OpenAiChatModel.builder().openAiApi(
//            OpenAiApi.builder().baseUrl("http://localhost:3000").
//                apiKey("sk-hTmGLyzDvnk8ouwBB7JFNJmzLibhIXwu6AMTX6UdxtEA3ddJ").build())
//        .defaultOptions(openAiChatOptions).build();
//
//    System.out.println(openAiChatModel.call("hello"));


//        OpenAiChatModel chatModel = new OpenAiChatModel(new OpenAiApi("http://localhost:3000", new ApiKey() {
//            @Override
//            public String getValue() {
//                return "";
//            }
//        }, ));

//        sk-hTmGLyzDvnk8ouwBB7JFNJmzLibhIXwu6AMTX6UdxtEA3ddJ

//    McpSyncClient client = McpClient.sync(new HttpClientSseClientTransport("https://mcp-openapi-to-mcp-adapter.onrender.com/sse?s=https%3A%2F%2Fraw.githubusercontent.com%2Fgithub%2Frest-api-description%2Fmain%2Fdescriptions%2Fapi.github.com%2Fapi.github.com.json&u=https%3A%2F%2Fapi.github.com&h=%7B%22Authorization%22%3A%22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%22%7D&f=%2B%2F**")).build();
//    McpSyncClient client = McpClient.sync(new HttpClientSseClientTransport("http://localhost:22015)).build();
//    McpSyncClient client = McpClient.sync(new HttpClientSseClientTransport.Builder("http://localhost:22015").sseEndpoint("/sse").build()).build();
//    McpSyncClient client = McpClient.sync(new HttpClientSseClientTransport.Builder("https://mcp-openapi-to-mcp-adapter.onrender.com").sseEndpoint("/sse?s=https%3A%2F%2Fraw.githubusercontent.com%2Fgithub%2Frest-api-description%2Fmain%2Fdescriptions%2Fapi.github.com%2Fapi.github.com.json&u=https%3A%2F%2Fapi.github.com&h=%7B%22Authorization%22%3A%22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%22%7D&f=%2B%2F**").build()).build();
    McpSyncClient client = McpClient.sync(new HttpClientSseClientTransport.Builder("https://mcp-openapi-to-mcp-adapter.onrender.com").sseEndpoint("/sse?s=https%3A%2F%2Fgithub.com%2Fautomation-ai-labs%2Fmcp-link%2Fraw%2Frefs%2Fheads%2Fmain%2Fexamples%2Fduckduckgo.yaml&u=https%3A%2F%2Fapi.duckduckgo.com&f=%2B%2F**").build()).build();
    McpSchema.InitializeResult initialize = client.initialize();
    McpSchema.ListToolsResult listToolsResult = client.listTools();
    System.out.println(listToolsResult.tools().size());

//    SyncMcpToolCallbackProvider syncMcpToolCallbackProvider = new SyncMcpToolCallbackProvider(List.of(client));
//    FunctionCallback[] toolCallbackArray = syncMcpToolCallbackProvider.getToolCallbacks();

    ArrayList<Object> toolCallbackssss = new ArrayList();
    List<McpSyncClient> client1 = List.of(client);
    client1.stream().forEach((mcpClient) -> toolCallbackssss.addAll(mcpClient.listTools().tools().stream().map((tool) -> new SyncMcpToolCallback(mcpClient, tool)).toList()));
    ToolCallback[] array = (ToolCallback[]) toolCallbackssss.toArray(new ToolCallback[0]);

    List<FunctionCallback> toolCallbacks = Arrays.asList(array);
//    List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames();
//    System.out.println(duplicateToolNames);
    //"pos_owner_repo_environments_environment_name_secrets_secret_name"

    System.out.println("------");
    listToolsResult.tools().stream().map(tool->tool.name()).forEach(System.out::println);
    System.out.println("------");

//    mcplink_github_v3_rest_api_get_repos_owner_repo_environments_environment_name_secrets_secret_name
//    mcplink_github_v3_rest_api_delete_repos_owner_repo_environments_environment_name_secrets_secret_name

    List<String> collect = toolCallbacks.stream()
        .collect(Collectors.groupingBy(FunctionCallback::getName, Collectors.counting()))
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue() > 1)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
//    System.out.println(listToolsResult.tools().get(0).name());

//    System.out.println(listToolsResult.tools());


    OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
    openAiChatOptions.setModel("gpt-4-turbo-preview");
    OpenAiChatModel openAiChatModel = OpenAiChatModel.builder().openAiApi(
            OpenAiApi.builder().baseUrl("http://localhost:3000").
                apiKey("sk-hTmGLyzDvnk8ouwBB7JFNJmzLibhIXwu6AMTX6UdxtEA3ddJ").build())
        .defaultOptions(openAiChatOptions).build();

//    System.out.println(openAiChatModel.call("hello"));
    SyncMcpToolCallbackProvider syncMcpToolCallbackProvider = new SyncMcpToolCallbackProvider(client);

    ChatClient chatClient = ChatClient.builder(openAiChatModel).defaultTools(syncMcpToolCallbackProvider).build();
    ChatClient.CallResponseSpec call = chatClient.prompt("百度今天的股价,对工具的请求，封装为json").call();

    System.out.println(call.content());

//    for (McpSchema.Tool tool : listToolsResult.tools()) {
//      McpSchema.CallToolResult callToolResult = client.callTool(new McpSchema.CallToolRequest(tool.name(), new HashMap<>() {{
//        put("city", "北京");
//      }}));
//      System.out.println(callToolResult.content());
//    }
  }
}
