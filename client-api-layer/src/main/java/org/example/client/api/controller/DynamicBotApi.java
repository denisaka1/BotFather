package org.example.client.api.controller;

import lombok.AllArgsConstructor;
import org.example.client.api.helper.ApiRequestHelper;
import org.example.data.layer.entities.Bot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@AllArgsConstructor
@Component
public class DynamicBotApi {
    private final ApiRequestHelper apiRequestHelper;
    private static final String BASE_URL = "http://localhost:8082/api/dynamic_bot";

    public Boolean sendMessage(SendMessage message) {
        return apiRequestHelper.post(
                BASE_URL + "/send_message",
                message,
                Boolean.class
        );
    }

    public Bot registerBot(Bot bot) {
        return apiRequestHelper.post(
                BASE_URL + "/register_bot",
                bot,
                Bot.class
        );
    }
}
