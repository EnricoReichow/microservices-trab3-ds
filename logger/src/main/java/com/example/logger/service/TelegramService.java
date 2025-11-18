package com.example.logger.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class TelegramService {

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String token, String chatId, String text) {
        String url = "https://api.telegram.org/bot" + token + "/sendMessage";

        Map<String, Object> body = Map.of("chat_id", chatId, "text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}