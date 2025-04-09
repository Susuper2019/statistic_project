import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

    @Test
    public void runEmbeddingModel() {
        OpenAiEmbeddingOptions openAiEmbeddingOptions = new OpenAiEmbeddingOptions();
        openAiEmbeddingOptions.setModel("BAAI/bge-m3");
        OpenAiEmbeddingModel openAiEmbeddingModel = new OpenAiEmbeddingModel(OpenAiApi.builder()
                .baseUrl("http://localhost:3000")
                .apiKey("sk-U0V3PEiyb4kzw3LBGeGYSBFs25D2S7zSA9TMMlrzF4BVGr7j")
                .build(), MetadataMode.EMBED, openAiEmbeddingOptions);
        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

//        InternalDriver driver =  new InternalDriver("bolt://localhost:7687", "neo4j", "your_password");

        Driver neo4j = GraphDatabase.driver("neo4j://localhost:7687",
                AuthTokens.basic("neo4j", "pearl-tribune-samba-buzzer-hope-2160"));
        Neo4jVectorStore vectorStore = Neo4jVectorStore.builder(neo4j, openAiEmbeddingModel)
                .databaseName("neo4j")                // Optional: defaults to "neo4j"
                .distanceType(Neo4jVectorStore.Neo4jDistanceType.COSINE) // Optional: defaults to COSINE
                .embeddingDimension(1024)
//                .dimensions(1536)                      // Optional: defaults to 1536
                .label("Document2")                     // Optional: defaults to "Document"
                .embeddingProperty("embedding2")        // Optional: defaults to "embedding"
                .indexName("custom-index2")             // Optional: defaults to "spring-ai-document-index"
                .initializeSchema(true)                // Optional: defaults to false
                .batchingStrategy(new TokenCountBatchingStrategy()) // Optional: defaults to TokenCountBatchingStrategy
                .build();

        vectorStore.add(documents);

        List<Document> documents1 = vectorStore.similaritySearch(SearchRequest.builder().query("Salvation").topK(1).build());
        for (Document document : documents1) {
            System.out.println(document);
        }


        OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
        openAiChatOptions.setModel("gpt-4-turbo-preview");
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder().openAiApi(
                        OpenAiApi.builder().baseUrl("http://localhost:3000").
                                apiKey("sk-U0V3PEiyb4kzw3LBGeGYSBFs25D2S7zSA9TMMlrzF4BVGr7j").build())
                .defaultOptions(openAiChatOptions).build();


        ChatResponse response = ChatClient.builder(openAiChatModel)
                .build().prompt()
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .advisors()
                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "type == 'Spring'"))
                .user("What is the meaning of life?")
                .call()
                .chatResponse();
        System.out.println(response.getResult());

        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        ChatResponse response2 = ChatClient.builder(openAiChatModel)
                .build().prompt()
                .advisors(retrievalAugmentationAdvisor)
                .user("What is the meaning of life?")
                .call()
                .chatResponse();
        System.out.println(response.getResult());

//        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
//                .documentRetriever(VectorStoreDocumentRetriever.builder()
//                        .similarityThreshold(0.50)
//                        .vectorStore(vectorStore)
//                        .build())
//                .build();
//        ChatClient.builder(openAiChatModel)
//                .build().prompt()
//                .advisors(new QuestionAn)

//        float[] iLikeSpringBoots = openAiEmbeddingModel.embed(documents);
//        System.out.println(Arrays.toString(iLikeSpringBoots));
    }
}
