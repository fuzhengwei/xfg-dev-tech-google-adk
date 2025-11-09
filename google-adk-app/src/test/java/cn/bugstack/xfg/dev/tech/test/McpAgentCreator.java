package cn.bugstack.xfg.dev.tech.test;

import com.google.adk.JsonBaseModel;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.mcp.McpTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.modelcontextprotocol.client.transport.ServerParameters;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class McpAgentCreator {

    @Test
    public void test() {
        String yourFolderPath = "/Users/fuzhengwei/Desktop";

        ServerParameters connectionParams = ServerParameters.builder("npx")
                .args(List.of(
                        "-y",
                        "@modelcontextprotocol/server-filesystem",
                        yourFolderPath
                ))
                .build();


        try {

            CompletableFuture<McpToolset.McpToolsAndToolsetResult> futureResult =
                    McpToolset.fromServer(connectionParams, JsonBaseModel.getMapper());

            McpToolset.McpToolsAndToolsetResult result = futureResult.join();

            try (McpToolset toolset = result.getToolset()) {
                List<McpTool> tools = result.getTools();

                LlmAgent agent = LlmAgent.builder()
                        .model("gemini-2.0-flash")
                        .name("enterprise_assistant")
                        .description("An agent to help users access their file systems")
                        .instruction(
                                "Help user accessing their file systems. You can list files in a directory."
                        )
                        .tools(tools)
                        .build();

                System.out.println("Agent created: " + agent.name());

                InMemoryRunner runner = new InMemoryRunner(agent);
                String userId = "user123";
                String sessionId = "1234";
                String promptText = "Which files are in this directory - " + yourFolderPath + "?";
                promptText = "in " + yourFolderPath + "create a file xxx.md";

                // Explicitly create the session first
                try {
                    // appName for InMemoryRunner defaults to agent.name() if not specified in constructor
                    runner.sessionService().createSession(runner.appName(), userId, null, sessionId).blockingGet();
                    System.out.println("Session created: " + sessionId + " for user: " + userId);
                } catch (Exception sessionCreationException) {
                    System.err.println("Failed to create session: " + sessionCreationException.getMessage());
                    sessionCreationException.printStackTrace();
                    return;
                }

                Content promptContent = Content.fromParts(Part.fromText(promptText));

                System.out.println("\nSending prompt: \"" + promptText + "\" to agent...\n");

                runner.runAsync(userId, sessionId, promptContent, RunConfig.builder().build())
                        .blockingForEach(event -> {
                            System.out.println("Event received: " + event.toJson());
                        });
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
