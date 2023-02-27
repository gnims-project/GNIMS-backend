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
public class SlackController {

    @Value("${slack.webhook}")
    private String url;

    @GetMapping("/slack/scheduler-task")
    public void sendTaskResult(String message) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        Map<String,Object> request = new HashMap<>();
        request.put("username", "gnims-bot");
        request.put("text", message);

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(request);
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}
