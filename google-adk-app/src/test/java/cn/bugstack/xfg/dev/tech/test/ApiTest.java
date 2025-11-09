package cn.bugstack.xfg.dev.tech.test;

import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.models.Gemini;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

public class ApiTest {

    /**
     * 可申请免费测试api
     * https://ai.google.dev/gemini-api/docs/quickstart?hl=zh-cn#apps-script
     */
    public static void main(String[] args) {
        LlmAgent agent = LlmAgent.builder()
                .name("test")
                .description("test agent help user do work")
                .model(Gemini.builder()
                        .apiClient(Client.builder()
                                .apiKey("AIzaSyDF6JnvFx7xWEsARSGosNmvTU3ZoCwo-mc")
                                .httpOptions(HttpOptions
                                        .builder()
                                        .baseUrl("https://generativelanguage.googleapis.com")
                                        .timeout(500000)
                                        .build())
                                .build())
                        .modelName("gemini-2.0-flash")
                        .build())
                .build();

        InMemoryRunner runner = new InMemoryRunner(agent);

        Session session = runner
                .sessionService()
                .createSession("test", "xiaofuge")
                .blockingGet();

        Flowable<Event> events = runner.runAsync("xiaofuge", session.id(), Content.fromParts(Part.fromText("hi agent can you help me")));

        System.out.print("\nAgent > ");
        events.blockingForEach(event -> System.out.println(event.stringifyContent()));

    }

}
