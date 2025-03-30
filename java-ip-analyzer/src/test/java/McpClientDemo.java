//import io.modelcontextprotocol.client.McpClient;
//import io.modelcontextprotocol.client.McpSyncClient;
//import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
//import io.modelcontextprotocol.spec.McpSchema;
//
//import java.util.HashMap;
//
//public class McpClientDemo {
//    public static void main(String[] args) {
//        McpSyncClient client = McpClient.sync(new HttpClientSseClientTransport("http://127.0.0.1:8000/sse")).build();
//        McpSchema.ListToolsResult listToolsResult = client.listTools();
//        System.out.println(listToolsResult.tools());
//
//        for (McpSchema.Tool tool : listToolsResult.tools()) {
//            McpSchema.CallToolResult callToolResult = client.callTool(new McpSchema.CallToolRequest(tool.name(), new HashMap<>() {{
//                put("city", "北京");
//            }}));
//            System.out.println(callToolResult.content());
//        }
//    }
//}
