package com.gnims.project.share.slack;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SlackMessageSender {

    @Value("${slack.webhook}")
    private String dailyToken;

    @Value("${slack.webhook.error-tracking}")
    private String errorToken;

    @GetMapping("/send-scheduler-task-result")
    public void sendTaskResult(String message) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = createMessageForm("gnims-daily-bot",
                "🕛\n" + message + "\n");
        restTemplate.exchange(dailyToken, HttpMethod.POST, entity, String.class);
    }

    @GetMapping("/send-error")
    public void trackError(String message) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = createMessageForm("gnims-error-tracking-bot",
                "🔥\n" + message + "\n");
        restTemplate.exchange(errorToken, HttpMethod.POST, entity, String.class);
    }

    private HttpEntity<Map<String, Object>> createMessageForm(String botName, String message) {
        Map<String,Object> request = new HashMap<>();
        request.put("username", botName);
        request.put("text", message);

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(request);
        return entity;
    }
}
