package cn.bugstack.xfg.dev.tech.config;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class AgentConfig {

    @Bean
    public BaseAgent baseAgent() {
        return LlmAgent.builder()
                .name("multi_tool_agent")
                .model("gemini-2.0-flash")
                .description("Interactive service agent for engineering tasks.")
//                .tools(Collections.emptyList())
                .build();
    }
}