package cn.bugstack.xfg.dev.tech.trigger;

import cn.bugstack.xfg.dev.tech.domain.AgentService;
import cn.bugstack.xfg.dev.tech.trigger.dto.ChatRequest;
import cn.bugstack.xfg.dev.tech.trigger.dto.ChatResponse;
import cn.bugstack.xfg.dev.tech.trigger.dto.CreateSessionRequest;
import cn.bugstack.xfg.dev.tech.trigger.dto.CreateSessionResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/trigger")
@CrossOrigin(origins = "*")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping(path = "/session", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateSessionResponse createSession(@RequestBody CreateSessionRequest req) {
        String sessionId = agentService.createOrGetSession(req.getName(), req.getUserId());
        return new CreateSessionResponse(sessionId);
    }

    @PostMapping(path = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@RequestBody ChatRequest req) {
        String sessionId = req.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = agentService.createOrGetSession(req.getName(), req.getUserId());
        }
        List<String> outputs = agentService.chat(req.getUserId(), sessionId, req.getMessage());
        return new ChatResponse(sessionId, String.join("\n", outputs));
    }
}