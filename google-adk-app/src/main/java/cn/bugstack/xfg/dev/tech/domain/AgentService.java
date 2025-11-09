package cn.bugstack.xfg.dev.tech.domain;

import com.google.adk.agents.BaseAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentService {

    private static final String NAME = "multi_tool_agent";

    private final InMemoryRunner runner;
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    public AgentService(BaseAgent baseAgent) {
        this.runner = new InMemoryRunner(baseAgent);
    }

    public String createOrGetSession(String userId) {
        return userSessions.computeIfAbsent(userId, uid -> {
            Session session = runner
                    .sessionService()
                    .createSession(NAME, userId)
                    .blockingGet();
            return session.id();
        });
    }

    public List<String> chat(String userId, String sessionId, String message) {
        Content userMsg = Content.fromParts(Part.fromText(message));
        Flowable<Event> events = runner.runAsync(userId, sessionId, userMsg);
        List<String> outputs = new ArrayList<>();
        events.blockingForEach(event -> outputs.add(event.stringifyContent()));
        return outputs;
    }

}