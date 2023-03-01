package com.gnims.project.share.slack;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SlackMessageSender {

    @Value("${slack.webhook}")
    private String dailyToken;

    @Value("${slack.webhook.error-tracking}")
    private String errorToken;

    @GetMapping("/send-scheduler-task-result")
    public void sendTaskResult(String message) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        Map<String,Object> request = new HashMap<>();
        request.put("username", "gnims-bot");
        request.put("text", "🕛\n" + message + "\n");

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(request);
        restTemplate.exchange(dailyToken, HttpMethod.POST, entity, String.class);
    }

    @GetMapping("/send-error")
    public void trackError(String message) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        Map<String,Object> request = new HashMap<>();
        request.put("username", "gnims-error-tracking-bot");
        request.put("text", "🔥\n" + message + "\n");

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(request);
        restTemplate.exchange(errorToken, HttpMethod.POST, entity, String.class);
    }
}
