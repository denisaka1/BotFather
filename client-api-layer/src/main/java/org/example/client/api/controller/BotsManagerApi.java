package org.example.client.api.controller;

import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@AllArgsConstructor
@Component
public class BotsManagerApi {
    private final ApiRequestHelper apiRequestHelper;
    private static final String BASE_URL = "http://localhost:8083/api/bots_manager";

    public Boolean sendMessage(SendMessage message) {
        return apiRequestHelper.post(
                BASE_URL + "/send_message",
                message,
                Boolean.class
        );
    }
}
