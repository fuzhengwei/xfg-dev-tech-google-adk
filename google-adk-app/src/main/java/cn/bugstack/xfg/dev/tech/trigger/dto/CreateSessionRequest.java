package cn.bugstack.xfg.dev.tech.trigger.dto;

public class CreateSessionRequest {
    private String name;
    private String userId;

    public CreateSessionRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}