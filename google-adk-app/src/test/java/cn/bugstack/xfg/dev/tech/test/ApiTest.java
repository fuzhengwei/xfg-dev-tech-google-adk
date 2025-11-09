package cn.bugstack.xfg.dev.tech.test;

import com.google.adk.agents.BaseAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Test
    public void testEnvLoading() {
        System.out.println(System.getenv("GOOGLE_API_KEY"));
    }

    @Test
    public void testMultiToolAgentWithSpringContainer() {
        System.out.println("Testing MultiToolAgent with Spring container management");
        
        // Get the ROOT_AGENT from MultiToolAgent
        BaseAgent rootAgent = MultiToolAgent.ROOT_AGENT;
        assertNotNull("ROOT_AGENT should not be null", rootAgent);
        
        // Test the agent with InMemoryRunner
        InMemoryRunner runner = new InMemoryRunner(rootAgent);
        Session session = runner
                .sessionService()
                .createSession("multi_tool_agent", "test_user")
                .blockingGet();
        
        // Test getCurrentTime functionality
        Content userMsg = Content.fromParts(Part.fromText("What time is it in New York?"));
        Flowable<Event> events = runner.runAsync("test_user", session.id(), userMsg);
        
        final boolean[] foundTimeResponse = {false};
        events.blockingForEach(event -> {
            String content = event.stringifyContent();
            System.out.println("Agent response: " + content);
            if (content.contains("time") && content.contains("New York")) {
                foundTimeResponse[0] = true;
            }
        });
        
        assertTrue("Should find time information in response", foundTimeResponse[0]);
        
        // Test getWeather functionality
        Content weatherMsg = Content.fromParts(Part.fromText("What's the weather in New York?"));
        Flowable<Event> weatherEvents = runner.runAsync("test_user", session.id(), weatherMsg);
        
        final boolean[] foundWeatherResponse = {false};
        weatherEvents.blockingForEach(event -> {
            String content = event.stringifyContent();
            System.out.println("Agent weather response: " + content);
            if (content.contains("weather") && content.contains("New York")) {
                foundWeatherResponse[0] = true;
            }
        });
        
        assertTrue("Should find weather information in response", foundWeatherResponse[0]);
        
        System.out.println("MultiToolAgent test completed successfully");
    }

}
