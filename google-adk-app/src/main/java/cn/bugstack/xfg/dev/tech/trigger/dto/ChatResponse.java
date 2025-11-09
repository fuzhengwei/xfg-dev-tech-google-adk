package cn.bugstack.xfg.dev.tech.trigger.dto;

public class ChatResponse {
    private String sessionId;
    private String reply;

    public ChatResponse() {
    }

    public ChatResponse(String sessionId, String reply) {
        this.sessionId = sessionId;
        this.reply = reply;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}